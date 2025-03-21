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

import javax.inject.Inject
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Format
import play.api.mvc.Request
import play.api.Logging
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions._
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.RepeatingPageParams

trait Rendering extends Logging {
  def render(bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
    request: Request[_],
    messages: Messages,
    appConfig: AppConfig
  ): Html
}

trait FormRendering[T] {
  def render(form: Form[T], bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
    request: Request[_],
    messages: Messages,
    appConfig: AppConfig
  ): Html
}

trait RepeatedFormRendering[T] {
  def render(
    form: Form[T],
    bpr: BusinessRegistrationDetails,
    navigation: Navigation,
    sectionId: String,
    params: RepeatingPageParams
  )(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html
}

trait Page[T] extends Rendering {
  val id: String
  val format: Format[T]
  val data: Option[T]
  val withSubsection: PartialFunction[Option[String], Page[T]]

  def withData(data: T): Page[T]

  def parseFromRequest[X](withErrors: Rendering => X, withData: Page[T] => X)(implicit r: Request[_]): X

  def nextSubsection: Option[String]
  def previousSubsection: Option[String]
  def section: Option[String]

  /** returns None if the page can not be deleted or Some(new page state) */
  def delete: Option[Page[T]]

  def pageStatus: PageStatus
  def lastSection: Option[String]

  def updatedAddresses: List[Address]
}

object Page {

  type AnyPage = Page[_]

  class InjectedPage @Inject() (views: Views) {

    import CompanyOfficer.companyOfficerFormat
    import CompanyRegistrationNumber.format
    import ContactPerson.format
    import DateOfIncorporation.format
    import BusinessPartner.businessPartnerFormat
    import ListWithTrackedChanges.listWithTrackedChangesFormat

    val timeAtCurrentAddressPostUrl =
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.FormPageController.save("mainBusinessAddress")

    val timeAtCurrentAddressPage = new BasicPage[String](
      "years-at-current-address",
      MainBusinessAddressForm.timeAtCurrentAddressForm,
      new FormRendering[String] {
        override def render(form: Form[String], bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
          request: Request[_],
          messages: Messages,
          appConfig: AppConfig
        ): Html =
          views.main_business_address_years_at_address(form, bpr, navigation, timeAtCurrentAddressPostUrl)
      }
    )

    val anyPreviousBusinessAddressPostUrl =
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.FormPageController
        .saveWithSection("mainBusinessAddress", "any-previous-business-address")

    val anyPreviousBusinessAddressPage = new BasicPage[Boolean](
      "any-previous-business-address",
      MainBusinessAddressForm.hasPreviousAddressForm,
      new FormRendering[Boolean] {
        override def render(form: Form[Boolean], bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
          request: Request[_],
          messages: Messages,
          appConfig: AppConfig
        ): Html =
          views.main_business_address_has_previous_address(form, bpr, navigation, anyPreviousBusinessAddressPostUrl)
      }
    )

    val previousBusinessAddressPostUrl =
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.FormPageController
        .saveWithSection("mainBusinessAddress", "previous-business-address")

    val previousBusinessAddressPage = new BasicPage[PreviousAddress](
      "previous-business-address",
      MainBusinessAddressForm.previousAddressForm,
      new FormRendering[PreviousAddress] {
        override def render(form: Form[PreviousAddress], bpr: BusinessRegistrationDetails, navigation: Navigation)(
          implicit
          request: Request[_],
          messages: Messages,
          appConfig: AppConfig
        ): Html =
          views.main_business_address_previous_address(form, bpr, navigation, previousBusinessAddressPostUrl)
      }
    )

    val mainBusinessAddressPage = MainBusinessAddressPage(
      timeAtCurrentAddressPage,
      anyPreviousBusinessAddressPage,
      previousBusinessAddressPage
    )

    val contactPersonPage: Page[ContactPerson] = new BasicPage[ContactPerson](
      "contactPerson",
      ContactPersonForm.contactPersonForm,
      new FormRendering[ContactPerson] {
        override def render(
          form: Form[ContactPerson],
          bpr: BusinessRegistrationDetails,
          navigation: Navigation
        )(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
          views.contact_person(form, bpr, navigation)
      },
      addressOnPage = _.otherUkContactAddress
    )

    val companyRegistrationNumberPage: Page[CompanyRegistrationNumber] = new BasicPage[CompanyRegistrationNumber](
      "companyRegistrationNumber",
      CompanyRegistrationNumberForm.companyRegistrationNumberForm,
      new FormRendering[CompanyRegistrationNumber] {
        override def render(
          form: Form[CompanyRegistrationNumber],
          bpr: BusinessRegistrationDetails,
          navigation: Navigation
        )(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
          views.company_registration_number(form, navigation)
      }
    )

    val nationalInsuranceNumberPage: Page[NationalInsuranceNumber] = new BasicPage[NationalInsuranceNumber](
      "nationalInsuranceNumber",
      NationalInsuranceNumberForm.nationalInsuranceNumberForm,
      new FormRendering[NationalInsuranceNumber] {
        override def render(
          form: Form[NationalInsuranceNumber],
          bpr: BusinessRegistrationDetails,
          navigation: Navigation
        )(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
          views.national_insurance_number(form, navigation)
      }
    )

    val dateOfIncorporationPage = new BasicPage[DateOfIncorporation](
      "dateOfIncorporation",
      DateOfIncorporationForm.dateOfIncorporationForm,
      new FormRendering[DateOfIncorporation] {
        override def render(
          form: Form[DateOfIncorporation],
          bpr: BusinessRegistrationDetails,
          navigation: Navigation
        )(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
          views.date_of_incorporation(form, navigation)
      }
    )

    val tradingNamePage = new BasicPage[TradingName](
      "tradingName",
      TradingNameForm.tradingNameForm,
      new FormRendering[TradingName] {
        override def render(form: Form[TradingName], bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
          request: Request[_],
          messages: Messages,
          appConfig: AppConfig
        ): Html =
          views.trading_name(form, navigation)
      }
    )

    val vatNumberPage = new BasicPage[VatNumber](
      "vatNumber",
      VatNumberForm.vatNumberForm,
      new FormRendering[VatNumber] {
        override def render(form: Form[VatNumber], bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
          request: Request[_],
          messages: Messages,
          appConfig: AppConfig
        ): Html =
          views.vat_registration(
            form,
            navigation,
            uk.gov.hmrc.fhregistrationfrontend.controllers.routes.FormPageController.save("vatNumber")
          )
      }
    )

    val businessPartnersPage = new RepeatingPage[BusinessPartner](
      "businessPartners",
      new RepeatedFormRendering[(BusinessPartner, Boolean)] {
        override def render(
          form: Form[(BusinessPartner, Boolean)],
          bpr: BusinessRegistrationDetails,
          navigation: Navigation,
          sectionId: String,
          params: RepeatingPageParams
        )(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
          views.business_partners(form, navigation, sectionId, params)
      },
      BusinessPartnersForm.businessPartnerMapping,
      minItems = 2,
      addressOnPage = { bp =>
        Some(bp.identification.address)
      }
    )

    val companyOfficersPage = RepeatingPage[CompanyOfficer](
      "companyOfficers",
      new RepeatedFormRendering[(CompanyOfficer, Boolean)] {
        override def render(
          form: Form[(CompanyOfficer, Boolean)],
          bpr: BusinessRegistrationDetails,
          navigation: Navigation,
          sectionId: String,
          params: RepeatingPageParams
        )(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
          views.company_officers(form, navigation, sectionId, params)
      },
      CompanyOfficersForm.companyOfficerMapping
    )

    val businessStatusPage = new BasicPage[BusinessStatus](
      "businessStatus",
      BusinessStatusForm.businessStatusForm,
      new FormRendering[BusinessStatus] {
        override def render(
          form: Form[BusinessStatus],
          bpr: BusinessRegistrationDetails,
          navigation: Navigation
        )(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
          views.business_status(form, navigation)
      }
    )

    val hasEoriPostUrl =
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.FormPageController.save("importingActivities")

    val hasEoriPage = new BasicPage[Boolean](
      "hasEori",
      ImportingActivitiesForm.hasEoriForm,
      new FormRendering[Boolean] {
        override def render(form: Form[Boolean], bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
          request: Request[_],
          messages: Messages,
          appConfig: AppConfig
        ): Html =
          views.has_eori(form, navigation, hasEoriPostUrl)
      }
    )

    val importingActivitiesEoriYesNumberPostUrl =
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.FormPageController
        .saveWithSection("importingActivities", "enterEORI")

    val importingActivitiesEoriYesNumberPage = new BasicPage[String](
      "enterEORI",
      ImportingActivitiesForm.eoriNumberOnlyForm,
      new FormRendering[String] {
        override def render(form: Form[String], bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
          request: Request[_],
          messages: Messages,
          appConfig: AppConfig
        ): Html =
          views.importing_activities_eori_yes_eori_number(form, navigation, importingActivitiesEoriYesNumberPostUrl)
      }
    )

    val importingActivitiesEoriYesGoodsPostUrl =
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.FormPageController
        .saveWithSection("importingActivities", "importingGoodsNotBelongingToBusiness")

    val importingActivitiesEoriYesGoodsPage = new BasicPage[Boolean](
      "importingGoodsNotBelongingToBusiness",
      ImportingActivitiesForm.goodsImportedOutsideEoriOnlyForm,
      new FormRendering[Boolean] {
        override def render(form: Form[Boolean], bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
          request: Request[_],
          messages: Messages,
          appConfig: AppConfig
        ): Html =
          views.importing_activities_eori_yes_goods(form, navigation, importingActivitiesEoriYesGoodsPostUrl)
      }
    )

    val importingActivitiesPage = ImportingActivitiesPage(
      hasEoriPage,
      importingActivitiesEoriYesNumberPage,
      importingActivitiesEoriYesGoodsPage
    )

    val businessCustomersPage = new BasicPage[BusinessCustomers](
      "businessCustomers",
      BusinessCustomersForm.businessCustomersForm,
      new FormRendering[BusinessCustomers] {
        override def render(
          form: Form[BusinessCustomers],
          bpr: BusinessRegistrationDetails,
          navigation: Navigation
        )(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
          views.business_customers(
            form,
            navigation,
            uk.gov.hmrc.fhregistrationfrontend.controllers.routes.FormPageController.save("businessCustomers")
          )
      }
    )

    val hasOtherStoragePremisesPage = BasicPage[Boolean](
      "hasOtherStoragePremises",
      StoragePremisesForm.hasOtherStoragePrmisesForm,
      new FormRendering[Boolean] {
        override def render(form: Form[Boolean], bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
          request: Request[_],
          messages: Messages,
          appConfig: AppConfig
        ): Html =
          views.has_other_storage_premises(form, navigation)
      }
    )

    val storagePremisesPage = RepeatingPage[StoragePremise](
      "storagePremises",
      new RepeatedFormRendering[(StoragePremise, Boolean)] {
        override def render(
          form: Form[(StoragePremise, Boolean)],
          bpr: BusinessRegistrationDetails,
          navigation: Navigation,
          sectionId: String,
          params: RepeatingPageParams
        )(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
          views.storage_premise(
            form,
            navigation,
            sectionId,
            params,
            uk.gov.hmrc.fhregistrationfrontend.controllers.routes.FormPageController
              .saveWithSection("otherStoragePremises", sectionId)
          )
      },
      StoragePremisesForm.storagePremiseMapping,
      addressOnPage = { sp =>
        Some(sp.address)
      }
    )

    val otherStoragePremisesPage = OtherStoragePremisesPage(
      hasOtherStoragePremisesPage,
      storagePremisesPage
    )
  }
}
