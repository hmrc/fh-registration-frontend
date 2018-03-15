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

import uk.gov.hmrc.fhregistrationfrontend.forms.models.{LimitedCompanyApplication, PartnershipApplication, SoleProprietorApplication}
import uk.gov.hmrc.http.cache.client.CacheMap

object Journeys {

  import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page._

  val limitedCompanyPages = new JourneyPages(
    Seq[AnyPage](
      Page.mainBusinessAddressPage,
      Page.contactPersonPage,
      Page.companyRegistrationNumberPage,
      Page.dateOfIncorporationPage,
      Page.tradingNamePage,
      Page.vatNumberPage,
      Page.companyOfficersPage,
      Page.businessStatusPage,
      Page.importingActivitiesPage,
      Page.businessCustomersPage,
      Page.otherStoragePremisesPage
    )
  )

  val soleTraderPages = new JourneyPages(
    Seq[Page[_]](
      Page.mainBusinessAddressPage,
      Page.contactPersonPage,
      Page.nationalInsuranceNumberPage,
      Page.tradingNamePage,
      Page.vatNumberPage,
      Page.businessStatusPage,
      Page.importingActivitiesPage,
      Page.businessCustomersPage,
      Page.otherStoragePremisesPage
    )
  )

  val partnershipPages = new JourneyPages(
    Seq[Page[_]](
      Page.mainBusinessAddressPage,
      Page.contactPersonPage,
      Page.tradingNamePage,
      Page.vatNumberPage,
      Page.businessPartnersPage,
      Page.businessStatusPage,
      Page.importingActivitiesPage,
      Page.businessCustomersPage,
      Page.otherStoragePremisesPage
    )
  )

  def unapplyLimitedCompanyApplication(a: LimitedCompanyApplication) = {
    new JourneyPages(
      Seq[AnyPage](
        mainBusinessAddressPage withData a.mainBusinessAddress,
        contactPersonPage withData a.contactPerson,
        companyRegistrationNumberPage withData a.companyRegistrationNumber,
        dateOfIncorporationPage withData a.dateOfIncorporation,
        tradingNamePage withData a.tradingName,
        vatNumberPage withData a.vatNumber,
        companyOfficersPage withData a.companyOfficers,
        businessStatusPage withData a.businessStatus,
        importingActivitiesPage withData a.importingActivities,
        businessCustomersPage withData a.businessCustomers,
        otherStoragePremisesPage withData a.otherStoragePremises
      )
    )
  }

  def unapplySoleTraderApplication(a: SoleProprietorApplication) = {
    new JourneyPages(
      Seq[Page[_]](
        mainBusinessAddressPage withData a.mainBusinessAddress,
        contactPersonPage withData a.contactPerson,
        nationalInsuranceNumberPage withData a.nationalInsuranceNumber,
        tradingNamePage withData a.tradingName,
        vatNumberPage withData a.vatNumber,
        businessStatusPage withData a.businessStatus,
        importingActivitiesPage withData a.importingActivities,
        businessCustomersPage withData a.businessCustomers,
        otherStoragePremisesPage withData a.otherStoragePremises
      )
    )
  }


  def unapplyPartnershipApplication(a: PartnershipApplication) = {
    new JourneyPages(
      Seq[Page[_]](
        mainBusinessAddressPage withData a.mainBusinessAddress,
        contactPersonPage withData a.contactPerson,
        tradingNamePage withData a.tradingName,
        vatNumberPage  withData a.vatNumber,
        businessPartnersPage withData a.businessPartners,
        businessStatusPage withData a.businessStatus,
        importingActivitiesPage withData a.importingActivities,
        businessCustomersPage withData a.businessCustomers,
        otherStoragePremisesPage withData a.otherStoragePremises

      )
    )
  }

  def partnershipApplication(pageDataLoader: PageDataLoader) =  PartnershipApplication(
      pageDataLoader pageData mainBusinessAddressPage,
      pageDataLoader pageData contactPersonPage,
      pageDataLoader pageData tradingNamePage,
      pageDataLoader pageData vatNumberPage,
      pageDataLoader pageData businessPartnersPage,
      pageDataLoader pageData businessStatusPage,
      pageDataLoader pageData importingActivitiesPage,
      pageDataLoader pageData businessCustomersPage,
      pageDataLoader pageData otherStoragePremisesPage
    )

  def soleTraderApplication(pageDataLoader: PageDataLoader) = SoleProprietorApplication(
    pageDataLoader pageData mainBusinessAddressPage,
    pageDataLoader pageData contactPersonPage,
    pageDataLoader pageData nationalInsuranceNumberPage,
    pageDataLoader pageData tradingNamePage,
    pageDataLoader pageData vatNumberPage,
    pageDataLoader pageData businessStatusPage,
    pageDataLoader pageData importingActivitiesPage,
    pageDataLoader pageData businessCustomersPage,
    pageDataLoader pageData otherStoragePremisesPage
  )

  def ltdApplication(pageDataLoader: PageDataLoader) = LimitedCompanyApplication(
      pageDataLoader pageData mainBusinessAddressPage,
      pageDataLoader pageData contactPersonPage,
      pageDataLoader pageData companyRegistrationNumberPage,
      pageDataLoader pageData dateOfIncorporationPage,
      pageDataLoader pageData tradingNamePage,
      pageDataLoader pageData vatNumberPage,
      pageDataLoader pageData companyOfficersPage,
      pageDataLoader pageData businessStatusPage,
      pageDataLoader pageData importingActivitiesPage,
      pageDataLoader pageData businessCustomersPage,
      pageDataLoader pageData otherStoragePremisesPage
    )

  def linearJourney(journeyPages: JourneyPages) = new LinearJourney(journeyPages)
  def summaryJourney(journeyPages: JourneyPages) = new SummaryJourney()

  def journeyState(journeyPages: JourneyPages, cachedMap: CacheMap) = new CachedJourneyState(cachedMap, journeyPages)

}
