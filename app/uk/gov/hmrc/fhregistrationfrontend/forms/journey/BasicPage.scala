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

import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Format
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails

case class BasicPage[T](id: String, form: Form[T], rendering: FormRendering[T], data: Option[T] = None)(implicit val format: Format[T])
  extends Page[T]
    with Rendering {

  override def withData(data: T) = this copy (data = Some(data))


  override def nextSubsection: Option[String] = None
  override def parseFromRequest[X](withErrors: Rendering ⇒ X, withData: Page[T] ⇒ X)(implicit r: Request[_]): X = {
    val updatedForm = form.bindFromRequest()
    if (updatedForm.hasErrors)
      withErrors(errorRenderer(updatedForm))
    else {
      val updatedPage = this.copy(data = updatedForm.value)
      withData(updatedPage)
    }
  }

  override def render(bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit request: Request[_], messages: Messages): Html = {
    val filledForm = data map (form fill _) getOrElse form
    rendering.render(filledForm, bpr, navigation)
  }


  private def errorRenderer(form: Form[T]) = new Rendering {
    override def render(bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit request: Request[_], messages: Messages): Html =
      rendering.render(form, bpr, navigation)
  }

  override val withSubsection: PartialFunction[Option[String], Page[T]] = {
    case None ⇒ this
  }

  override def delete: Option[Page[T]] = None

  override def pageStatus: PageStatus = if (data.isDefined) Completed else NotStarted

  override def lastSection: Option[String] = None
}