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

import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page.AnyPage
import uk.gov.hmrc.http.cache.client.CacheMap

object Journeys {

  val limitedCompanyPages = new JourneyPages(
    Seq[AnyPage](
      Page.mainBusinessAddressPage,
      Page.contactPersonPage,
      Page.companyRegistrationNumberPage,
      Page.dateOfIncorporationPage,
      Page.tradingNamePage,
      Page.vatNumberPage,
      Page.companyOfficerPage,
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


  def linearJourney(journeyPages: JourneyPages) = new LinearJourney(journeyPages)
  def summaryJourney(journeyPages: JourneyPages) = new SummaryJourney()

  def journeyState(journeyPages: JourneyPages, cachedMap: CacheMap) = new CachedJourneyState(cachedMap, journeyPages)


}
