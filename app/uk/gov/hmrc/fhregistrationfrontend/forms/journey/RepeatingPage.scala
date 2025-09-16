/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.fhregistrationfrontend.forms.journey

import play.api.data.{Form, FormError, Mapping}
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.json.Format
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, ListWithTrackedChanges}
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.RepeatingPageParams

import scala.util.Try

case class RepeatingPage[T](
  id: String,
  renderer: RepeatedFormRendering[(T, Boolean)],
  mapping: Mapping[T],
  value: ListWithTrackedChanges[T] = ListWithTrackedChanges.empty[T](),
  updatedAddresses: List[Address] = List.empty,
  index: Int = 0,
  minItems: Int = 1,
  maxItems: Int = 100,
  addressOnPage: T => Option[Address] = (_: T) => None
)(implicit val format: Format[ListWithTrackedChanges[T]])
    extends Page[ListWithTrackedChanges[T]] {

  val AddMoreKey = "addMore"
  val ElementKey = "element"

  val form: Form[(T, Boolean)] = Form(
    tuple(
      ElementKey -> skippingOnePrefix(mapping),
      AddMoreKey -> yesOrNo()
    )
  )

  override def withData(data: ListWithTrackedChanges[T]) = this.copy(value = data)

  override val withSubsection: PartialFunction[Option[String], Page[ListWithTrackedChanges[T]]] = {
    case None                       => this.copy(index = 0)
    case Some(v) if validSection(v) => this.copy(index = v.toInt - 1)
  }

  private def validSection(sectionId: String): Boolean =
    Try(sectionId.toInt)
      .map(s => s >= 1 && s <= value.size + 1 && s <= maxItems)
      .getOrElse(false)

  override def nextSubsection: Option[String] =
    if (value.addMore)
      Some(section(value.size))
//    else if (index < value.size - 1)
//      Some(section(index + 1))
    else
      None

  override def previousSubsection: Option[String] =
    if (index == 0) None
    else Some(section(index - 1))

  def section(index: Int) = (index + 1).toString

  override def parseFromRequest[X](onErrors: Rendering => X, onSuccess: Page[ListWithTrackedChanges[T]] => X)(implicit
    r: Request[?]
  ): X = {
    import play.api.data.FormBinding.Implicits._
    val updatedForm = form.bindFromRequest()
    if (updatedForm.hasErrors)
      onErrors(errorRenderer(updatedForm))
    else if (index > maxItems)
      onErrors(errorRenderer(updatedForm withError (ElementKey, "too.many.items")))
    else {
      val (element, more): (T, Boolean) = updatedForm.value.get
      if (more && (index + 1 > maxItems))
        onErrors(errorRenderer(updatedForm withError (AddMoreKey, "too.many.items")))
      else {
        val updateValue =
          if (value.size <= index) value `append` element
          else value.updated(index, element)

        val updatedAddress =
          if (value.size <= index) addressOnPage(element)
          else if (addressOnPage(value(index)) != addressOnPage(element)) addressOnPage(element)
          else None

        val updatePage =
          this.copy(value = updateValue.copy(addMore = more), updatedAddresses = updatedAddress.toList)
        onSuccess(updatePage)
      }
    }
  }

  def renderWithFormError(
    formError: Seq[FormError],
    bpr: BusinessRegistrationDetails,
    navigation: Navigation
  )(implicit
    request: Request[?],
    messages: Messages,
    appConfig: AppConfig
  ): Html = {
    val filledForm =
      if (index < value.size) form fill ((value(index), value.addMore))
      else form

    val formWithError = filledForm.copy(errors = formError)
    renderer.render(formWithError, bpr, navigation, section(index), renderingParams)
  }

  override def render(bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
    request: Request[?],
    messages: Messages,
    appConfig: AppConfig
  ): Html = {
    val filledForm =
      if (index < value.size) form fill ((value(index), value.addMore))
      else form

    renderer.render(filledForm, bpr, navigation, section(index), renderingParams)
  }

  private def errorRenderer(form: Form[(T, Boolean)]) = new Rendering {
    override def render(bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
      request: Request[?],
      messages: Messages,
      appConfig: AppConfig
    ): Html =
      renderer.render(form, bpr, navigation, section(index), renderingParams)
  }

  def renderingParams = RepeatingPageParams(
    canRemove = value.size > minItems && index < value.size,
    forceHasMore =
      if (math.max(value.size, index + 1) < minItems) Some(true)
      else if (math.max(value.size, index + 1) >= maxItems) Some(false)
      else None
  )

  override val data: Option[ListWithTrackedChanges[T]] = Some(value)

  override def delete: Option[Page[ListWithTrackedChanges[T]]] =
    if (value.size <= minItems)
      None
    else
      Some(this.copy(value = value `remove` index))

  override def pageStatus: PageStatus =
    if (value.size == 0 && minItems > 0) NotStarted
    else if (value.size < minItems) InProgress
    else if (value.addMore) InProgress
    else Completed

  override def lastSection: Option[String] =
    if (value.size == 0)
      None
    else
      Some(section(value.size - 1))

  override def section = Some(section(index))
}
