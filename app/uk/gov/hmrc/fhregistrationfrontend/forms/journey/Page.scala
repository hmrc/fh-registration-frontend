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
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions._
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.views.html.forms._


trait Rendering[T] {
  def render(form: Form[T], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)(implicit request: Request[_], messages: Messages): Html
}

trait JsonFormatting[T] {
  val format: Format[T]
}


trait Page[T] extends Rendering[T] with JsonFormatting[T] {
  val id: String
  val form: Form[T]
}

abstract class BasicPage[T](val id: String, val form: Form[T])(implicit val format: Format[T])
  extends Page[T]
    with JsonFormatting[T]
    with Rendering[T]

object Page {
  import CompanyRegistrationNumber.format
  import ContactPerson.format
  import DateOfIncorporation.format
  import MainBusinessAddress.format

  val mainBusinessAddressPage = new BasicPage[MainBusinessAddress](
    "mainBusinessAddress", MainBusinessAddressForm.mainBusinessAddressForm
  ) {
    override def render(form: Form[MainBusinessAddress], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)
      (implicit request: Request[_], messages: Messages): Html = {
      main_business_address(form, bpr, navigation, formType)
    }
  }

  val contactPersonPage = new BasicPage[ContactPerson](
    "contactPerson", ContactPersonForm.contactPersonForm
  ) {
    override def render(form: Form[ContactPerson], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)
      (implicit request: Request[_], messages: Messages): Html = {
      contact_person(form, bpr, navigation, formType)
    }
  }

  val companyRegistrationNumberPage = new BasicPage[CompanyRegistrationNumber](
    "companyRegistrationNumber", CompanyRegistrationNumberForm.companyRegistrationNumberForm
  ) {
    override def render(form: Form[CompanyRegistrationNumber], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)
      (implicit request: Request[_], messages: Messages): Html = {
      company_registration_number(form, navigation, formType)
    }
  }

  val nationalInsuranceNumberPage = new BasicPage[NationalInsuranceNumber](
    "nationalInsuranceNumber", NationalInsuranceNumberForm.nationalInsuranceNumberForm
  ) {
    override def render(form: Form[NationalInsuranceNumber], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)
      (implicit request: Request[_], messages: Messages): Html = {
      national_insurance_number(form, navigation, formType)
    }
  }

  val dateOfIncorporationPage = new BasicPage[DateOfIncorporation](
    "dateOfIncorporation", DateOfIncorporationForm.dateOfIncorporationForm
  ) {
    override def render(form: Form[DateOfIncorporation], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)
      (implicit request: Request[_], messages: Messages): Html = {
      date_of_incorporation(form, navigation, formType)
    }
  }

  val tradingNamePage = new BasicPage[TradingName](
    "tradingName", TradingNameForm.tradingNameForm
  ) {
    override def render(form: Form[TradingName], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)
      (implicit request: Request[_], messages: Messages): Html = {
      trading_name(form, navigation, formType)
    }
  }

  val vatNumberPage = new BasicPage[VatNumber]("vatNumber", VatNumberForm.vatNumberForm) {
    override def render(form: Form[VatNumber], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)
      (implicit request: Request[_], messages: Messages): Html = {
      vat_registration(form, navigation, formType)
    }
  }

  //todo: should use CompanyOfficersForm.companyOfficersForm. When repeat component ready, replace companyOfficerForm with companyOfficersForm
  val companyOfficerPage = new BasicPage[CompanyOfficer](
    "companyOfficers", CompanyOfficersForm.companyOfficerForm
  ) {
    override def render(form: Form[CompanyOfficer], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)
      (implicit request: Request[_], messages: Messages): Html = {
      company_officers(form, navigation, formType)
    }
  }

  val businessStatusPage = new BasicPage[BusinessStatus](
    "businessStatus", BusinessStatusForm.businessStatusForm
  ) {
    override def render(form: Form[BusinessStatus], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)
      (implicit request: Request[_], messages: Messages): Html = {
      business_status(form, navigation, formType)
    }
  }

  val importingActivitiesPage = new BasicPage[ImportingActivities](
    "importingActivities", ImportingActivitiesForm.importingActivitiesForm
  ) {
    override def render(form: Form[ImportingActivities], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)
      (implicit request: Request[_], messages: Messages): Html = {
      importing_activities(form, navigation, formType)
    }
  }

  val businessCustomersPage = new BasicPage[BusinessCustomers](
    "businessCustomers", BusinessCustomersForm.businessCustomersForm
  ) {
    override def render(form: Form[BusinessCustomers], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)
      (implicit request: Request[_], messages: Messages): Html = {
      business_customers(form, navigation, formType)
    }
  }

  //todo: should use StoragePremisesForm.storagePremisesForm. When repeat component ready, replace storagePremiseForm with storagePremisesForm

  val otherStoragePremisesPage = new BasicPage[OtherStoragePremises]("otherStoragePremises", StoragePremisesForm.storagePremisesForm) {
    override def render(form: Form[OtherStoragePremises], bpr: BusinessRegistrationDetails, navigation: Navigation, formType: String)
      (implicit request: Request[_], messages: Messages): Html = {
      other_storage_premises(form, navigation, formType)
    }
  }
}
