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

package uk.gov.hmrc.fhregistrationfrontend.actions

import play.api.test.FakeRequest
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{CacheMapBuilder, FormTestData, UserTestData}

trait JourneyRequestBuilder {

  this: UserTestData ⇒

  def partiallyCompleteJourney = {
    val pagesWithData = Journeys.partnershipPages map { page ⇒
      page.id match {
        case Page.contactPersonPage.id       ⇒ page.asInstanceOf[Page[ContactPerson]] withData FormTestData.contactPerson
        case Page.mainBusinessAddressPage.id ⇒ page.asInstanceOf[Page[MainBusinessAddress]] withData FormTestData.mainBusinessAddress
        case _                               ⇒ page
      }
    }
    new JourneyPages(pagesWithData)
  }

  def partialJourneyWithSection = {
    val pagesWithData = Journeys.partnershipPages map { page ⇒
      page.id match {
        case Page.contactPersonPage.id        => page.asInstanceOf[Page[ContactPerson]] withData FormTestData.contactPerson
        case Page.mainBusinessAddressPage.id  => page.asInstanceOf[Page[MainBusinessAddress]] withData FormTestData.mainBusinessAddress
        case Page.tradingNamePage.id          => page.asInstanceOf[Page[TradingName]] withData FormTestData.tradingName
        case Page.vatNumberPage.id            => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
        case Page.businessPartnersPage.id     => page.asInstanceOf[Page[ListWithTrackedChanges[BusinessPartner]]] withData FormTestData.partners.copy(addMore = true)
        case _ ⇒ page
      }
    }
    new JourneyPages(pagesWithData)
  }

  def fullyCompleteJourney = {
    val pagesWithData = Journeys.partnershipPages map { page ⇒
      page.id match {
        case Page.contactPersonPage.id        => page.asInstanceOf[Page[ContactPerson]] withData FormTestData.contactPerson
        case Page.mainBusinessAddressPage.id  => page.asInstanceOf[Page[MainBusinessAddress]] withData FormTestData.mainBusinessAddress
        case Page.tradingNamePage.id          => page.asInstanceOf[Page[TradingName]] withData FormTestData.tradingName
        case Page.vatNumberPage.id            => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
        case Page.businessPartnersPage.id     => page.asInstanceOf[Page[ListWithTrackedChanges[BusinessPartner]]] withData FormTestData.partners
        case Page.businessStatusPage.id       => page.asInstanceOf[Page[BusinessStatus]] withData FormTestData.businessStatus
        case Page.importingActivitiesPage.id  => page.asInstanceOf[Page[ImportingActivities]] withData FormTestData.importingActivities
        case Page.businessCustomersPage.id    => page.asInstanceOf[Page[BusinessCustomers]] withData FormTestData.businessCustomers
        case Page.otherStoragePremisesPage.id => page.asInstanceOf[Page[OtherStoragePremises]] withData FormTestData.otherStoragePremises
      }
    }
    new JourneyPages(pagesWithData)
  }

  def journeyRequest(
    userRequest: UserRequest[_] = new UserRequest(testUserId, None, None, FakeRequest()),
    journeyPages: JourneyPages = new JourneyPages(Journeys.partnershipPages)) = {

    new JourneyRequest(
      CacheMapBuilder(testUserId).cacheMap,
      userRequest,
      FormTestData.someBpr,
      BusinessType.Partnership,
      ggEmail,
      JourneyType.Amendment,
      journeyPages,
      new CachedJourneyState(journeyPages)
    )
  }
}


object JourneyRequestBuilder extends UserTestData with JourneyRequestBuilder {

}
