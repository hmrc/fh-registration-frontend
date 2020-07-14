/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page.AnyPage
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessEntityApplication, LimitedCompanyApplication, PartnershipApplication, SoleProprietorApplication}
import uk.gov.hmrc.fhregistrationfrontend.views.Views

class Journeys @Inject()(views: Views) {

  import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page.InjectedPage

  private val page = new InjectedPage(views)

  val limitedCompanyPages =
    Seq[AnyPage](
      page.contactPersonPage,
      page.mainBusinessAddressPage,
      page.companyRegistrationNumberPage,
      page.dateOfIncorporationPage,
      page.tradingNamePage,
      page.vatNumberPage,
      page.companyOfficersPage,
      page.businessStatusPage,
      page.importingActivitiesPage,
      page.businessCustomersPage,
      page.otherStoragePremisesPage
    )

  val soleTraderPages =
    Seq[AnyPage](
      page.contactPersonPage,
      page.mainBusinessAddressPage,
      page.nationalInsuranceNumberPage,
      page.tradingNamePage,
      page.vatNumberPage,
      page.businessStatusPage,
      page.importingActivitiesPage,
      page.businessCustomersPage,
      page.otherStoragePremisesPage
    )

  val partnershipPages =
    Seq[AnyPage](
      page.contactPersonPage,
      page.mainBusinessAddressPage,
      page.tradingNamePage,
      page.vatNumberPage,
      page.businessPartnersPage,
      page.businessStatusPage,
      page.importingActivitiesPage,
      page.businessCustomersPage,
      page.otherStoragePremisesPage
    )

  def unapplyApplication(application: BusinessEntityApplication): JourneyPages =
    application match {
      case a: LimitedCompanyApplication ⇒ this unapplyLimitedCompanyApplication a
      case a: SoleProprietorApplication ⇒ this unapplySoleTraderApplication a
      case a: PartnershipApplication ⇒ this unapplyPartnershipApplication a
    }

  def unapplyLimitedCompanyApplication(a: LimitedCompanyApplication) =
    new JourneyPages(
      Seq[AnyPage](
        page.mainBusinessAddressPage withData a.mainBusinessAddress,
        page.contactPersonPage withData a.contactPerson,
        page.companyRegistrationNumberPage withData a.companyRegistrationNumber,
        page.dateOfIncorporationPage withData a.dateOfIncorporation,
        page.tradingNamePage withData a.tradingName,
        page.vatNumberPage withData a.vatNumber,
        page.companyOfficersPage withData a.companyOfficers,
        page.businessStatusPage withData a.businessStatus,
        page.importingActivitiesPage withData a.importingActivities,
        page.businessCustomersPage withData a.businessCustomers,
        page.otherStoragePremisesPage withData a.otherStoragePremises
      )
    )

  def unapplySoleTraderApplication(a: SoleProprietorApplication) =
    new JourneyPages(
      Seq[AnyPage](
        page.mainBusinessAddressPage withData a.mainBusinessAddress,
        page.contactPersonPage withData a.contactPerson,
        page.nationalInsuranceNumberPage withData a.nationalInsuranceNumber,
        page.tradingNamePage withData a.tradingName,
        page.vatNumberPage withData a.vatNumber,
        page.businessStatusPage withData a.businessStatus,
        page.importingActivitiesPage withData a.importingActivities,
        page.businessCustomersPage withData a.businessCustomers,
        page.otherStoragePremisesPage withData a.otherStoragePremises
      )
    )

  def unapplyPartnershipApplication(a: PartnershipApplication) =
    new JourneyPages(
      Seq[AnyPage](
        page.mainBusinessAddressPage withData a.mainBusinessAddress,
        page.contactPersonPage withData a.contactPerson,
        page.tradingNamePage withData a.tradingName,
        page.vatNumberPage withData a.vatNumber,
        page.businessPartnersPage withData a.businessPartners,
        page.businessStatusPage withData a.businessStatus,
        page.importingActivitiesPage withData a.importingActivities,
        page.businessCustomersPage withData a.businessCustomers,
        page.otherStoragePremisesPage withData a.otherStoragePremises
      )
    )

  def partnershipApplication(pageDataLoader: PageDataLoader) = PartnershipApplication(
    pageDataLoader pageData page.mainBusinessAddressPage,
    pageDataLoader pageData page.contactPersonPage,
    pageDataLoader pageData page.tradingNamePage,
    pageDataLoader pageData page.vatNumberPage,
    pageDataLoader pageData page.businessPartnersPage,
    pageDataLoader pageData page.businessStatusPage,
    pageDataLoader pageData page.importingActivitiesPage,
    pageDataLoader pageData page.businessCustomersPage,
    pageDataLoader pageData page.otherStoragePremisesPage
  )

  def soleTraderApplication(pageDataLoader: PageDataLoader) = SoleProprietorApplication(
    pageDataLoader pageData page.mainBusinessAddressPage,
    pageDataLoader pageData page.contactPersonPage,
    pageDataLoader pageData page.nationalInsuranceNumberPage,
    pageDataLoader pageData page.tradingNamePage,
    pageDataLoader pageData page.vatNumberPage,
    pageDataLoader pageData page.businessStatusPage,
    pageDataLoader pageData page.importingActivitiesPage,
    pageDataLoader pageData page.businessCustomersPage,
    pageDataLoader pageData page.otherStoragePremisesPage
  )

  def ltdApplication(pageDataLoader: PageDataLoader) = LimitedCompanyApplication(
    pageDataLoader pageData page.mainBusinessAddressPage,
    pageDataLoader pageData page.contactPersonPage,
    pageDataLoader pageData page.companyRegistrationNumberPage,
    pageDataLoader pageData page.dateOfIncorporationPage,
    pageDataLoader pageData page.tradingNamePage,
    pageDataLoader pageData page.vatNumberPage,
    pageDataLoader pageData page.companyOfficersPage,
    pageDataLoader pageData page.businessStatusPage,
    pageDataLoader pageData page.importingActivitiesPage,
    pageDataLoader pageData page.businessCustomersPage,
    pageDataLoader pageData page.otherStoragePremisesPage
  )

  def linearJourney(journeyPages: JourneyPages) = new LinearJourney(journeyPages)
  def summaryJourney(journeyPages: JourneyPages) = new SummaryJourney()

  def journeyState(journeyPages: JourneyPages) = new CachedJourneyState(journeyPages)

}
