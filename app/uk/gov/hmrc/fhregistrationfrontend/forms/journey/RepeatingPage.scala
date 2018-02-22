/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.data.{Form, Mapping}
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.json.Format
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings._

import scala.util.Try


case class RepeatingPage[T](
  id      : String,
  renderer: RepeatedFormRendering[(T, Boolean)],
  mapping: Mapping[T],
  value   : List[T] = List.empty,
  index   : Int = 0,
  addMore : Boolean = false
)(implicit val format: Format[List[T]]) extends Page[List[T]] {

  val form: Form[(T, Boolean)] = Form(
    tuple(
      "element" → skippingOnePrefix(mapping),
      "addMore" → yesOrNo
    )
  )

  override def withData(data: List[T]) = this copy (value = data)

  override val withSubsection: PartialFunction[Option[String], Page[List[T]]] = {
    case None ⇒ this copy (index = 0)
    case Some(v) if validSection(v) ⇒ this copy (index = v.toInt - 1)
  }

  private def validSection(sectionId: String): Boolean = {
    Try(sectionId.toInt)
      .map(s ⇒ s >= 1 && s <= value.size + 1)
      .getOrElse(false)
  }

  override def nextSubsection: Option[String] =
    if (addMore)
      Some(section(value.length))
    else if (index < value.length - 1)
       Some(section(index + 1))
    else
      None

    Some(section(index + 1))
  def section(index: Int) = (index + 1).toString

  override def parseFromRequest[X](withErrors: Rendering ⇒ X, withData: Page[List[T]] ⇒ X)(implicit r: Request[_]): X = {
    val updatedForm = form.bindFromRequest
    if (updatedForm.hasErrors)
      withErrors(errorRenderer(updatedForm))
    else {
      val (element, more) = updatedForm.value.get
      val updateValue =
        if (value.length <= index) value :+ element
        else value.updated(index, element)
      val updatePage = this copy (value = updateValue, addMore = more)
      withData(updatePage)
    }
  }

  override def render(bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit request: Request[_], messages: Messages): Html = {
    val filledForm = if (index < value.size) form fill ((value(index), false)) else form

    renderer.render(filledForm, bpr, navigation, section(index))
  }

  private def errorRenderer(form: Form[(T, Boolean)]) = new Rendering {
    override def render(bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit request: Request[_], messages: Messages): Html = {
      renderer.render(form, bpr, navigation, section(index))
    }
  }

  override val data: Option[List[T]] = Some(value)

//  override def delete: Page[CompanyOfficers, CompanyOfficer] = {
//    if (index < 0 || index >= value.values.size)
//      this
//    else if (index == 0) {
//      this copy (value = value.values.tail)
//    }
//
//  }
}
