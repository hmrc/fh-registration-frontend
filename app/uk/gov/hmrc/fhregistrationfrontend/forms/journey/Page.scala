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
import play.api.mvc.{Request, Result}
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.{CompanyRegistrationNumberForm, ContactPersonForm, DateOfIncorporationForm, MainBusinessAddressForm}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{CompanyRegistrationNumber, ContactPerson, DateOfIncorporation, MainBusinessAddress}
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.views.html.forms._



trait Rendering[T] {
  def render(form: Form[T], bpr: BusinessRegistrationDetails)(implicit request: Request[_], messages: Messages): Html
}

trait JsonFormatting[T] {
  val format: Format[T]
}


trait Page[T] extends Rendering[T] with JsonFormatting[T] {
  val id: String
  val form: Form[T]
}

//trait BprLoading[T] extends Rendering[T] {
//  val save4LaterService: Save4LaterService
//  val renderWithBpr: (Form[T], BusinessRegistrationDetails) ⇒ Html
//  override val render:
//}

abstract class BasicPage[T](val id: String, val form: Form[T])(implicit val format: Format[T])
  extends Page[T]
    with JsonFormatting[T]
    with Rendering[T]

object Page {
  import MainBusinessAddress.format
  import CompanyRegistrationNumber.format
  import DateOfIncorporation.format
  import ContactPerson.format

  val mainBusinessAddressPage = new BasicPage[MainBusinessAddress](
    "mainBusinessAddress",
    MainBusinessAddressForm.mainBusinessAddressForm
  ) {
    override def render(form: Form[MainBusinessAddress], bpr: BusinessRegistrationDetails)(implicit request: Request[_], messages: Messages): Html = {
      println(s"rendering form $form")
      main_business_address(form, bpr)
    }
  }

  val contactPersonPage = new BasicPage[ContactPerson]("contactPerson", ContactPersonForm.contactPersonForm) {
    override def render(form: Form[ContactPerson], bpr: BusinessRegistrationDetails)(implicit request: Request[_], messages: Messages): Html = {
      contact_person(form, bpr)
    }
  }

  val companyRegistrationNumberPage = new BasicPage[CompanyRegistrationNumber]("companyRegistrationNumber", CompanyRegistrationNumberForm.companyRegistrationNumberForm) {
    override def render(form: Form[CompanyRegistrationNumber], bpr: BusinessRegistrationDetails)(implicit request: Request[_], messages: Messages): Html = {
      company_registration_number(form)
    }
  }

  val dateOfIncorporationPage = new BasicPage[DateOfIncorporation]("dateOfIncorporation", DateOfIncorporationForm.dateOfIncorporationForm) {
    override def render(form: Form[DateOfIncorporation], bpr: BusinessRegistrationDetails)(implicit request: Request[_], messages: Messages): Html = {
      date_of_incorporation(form)
    }

  }



}
