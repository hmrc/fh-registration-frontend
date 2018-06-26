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

import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.controllers.routes
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{JourneyType, Journeys, Page}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterKeys
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{CacheMapBuilder, FormTestData, Save4LaterMocks, StubbedErrorHandler}

class JourneyActionSpec extends ActionSpecBase with Save4LaterMocks with BeforeAndAfterEach {

  lazy val action = new JourneyAction()(mockSave4Later, StubbedErrorHandler)
  val userRequest = new UserRequest(testUserId, None, None, FakeRequest())

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSave4Later)
  }

  "JourenyAction" should {
    "Fail when no bpr" in {

      val cacheMap = CacheMapBuilder(testUserId).cacheMap
      setupSave4LaterFrom(cacheMap)

      status(result(action, userRequest)) shouldBe BAD_REQUEST

    }

    "Fail when no business type" in {
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.businessRegistrationDetailsKey, FormTestData.someBpr)
        .cacheMap
      setupSave4LaterFrom(cacheMap)

      status(result(action, userRequest)) shouldBe BAD_REQUEST

    }

    "Redirect to email verification when no verified email" in {
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.businessRegistrationDetailsKey, FormTestData.someBpr)
        .withValue(Save4LaterKeys.businessTypeKey, BusinessType.CorporateBody)
        .cacheMap
      setupSave4LaterFrom(cacheMap)

      val r = result(action, userRequest)
      status(r) shouldBe SEE_OTHER
      redirectLocation(r) shouldBe Some(routes.EmailVerificationController.emailVerificationStatus().url)

    }

    "Load journey with no page yet saved" in {
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.businessRegistrationDetailsKey, FormTestData.someBpr)
        .withValue(Save4LaterKeys.businessTypeKey, BusinessType.CorporateBody)
        .withValue(Save4LaterKeys.verifiedEmailKey, ggEmail)
        .cacheMap
      setupSave4LaterFrom(cacheMap)

      val refined = refinedRequest(action, userRequest)

      refined.userId shouldBe testUserId
      refined.registrationNumber shouldBe None
      refined.lastUpdateTimestamp shouldBe 0L
      refined.verifiedEmail shouldBe ggEmail
      refined.journeyType shouldBe JourneyType.New
      refined.businessType shouldBe BusinessType.CorporateBody
      refined.bpr shouldBe FormTestData.someBpr

      refined.journeyState.isComplete shouldBe false
      refined.journeyState.nextPageToComplete() shouldBe Some(Journeys.limitedCompanyPages.head.id)
      refined.journeyState.lastEditedPage shouldBe None

      refined.journeyPages.pages.map(_.id) shouldBe Journeys.limitedCompanyPages.map(_.id)
    }

    "Load journey with several pages saved" in {
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.businessRegistrationDetailsKey, FormTestData.someBpr)
        .withValue(Save4LaterKeys.businessTypeKey, BusinessType.CorporateBody)
        .withValue(Save4LaterKeys.verifiedEmailKey, ggEmail)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, 1529327782000L)
        .withValue(Page.contactPersonPage.id, FormTestData.contactPerson)
        .withValue(Page.mainBusinessAddressPage.id, FormTestData.mainBusinessAddress)
        .cacheMap
      setupSave4LaterFrom(cacheMap)

      val refined = refinedRequest(action, userRequest)

      refined.hasUpdates shouldBe None
      refined.lastUpdateTimestamp shouldBe 1529327782000L
      refined.journeyState.isComplete shouldBe false
      refined.journeyState.nextPageToComplete() shouldBe Some(Journeys.limitedCompanyPages(2).id)
      refined.journeyState.lastEditedPage.map(_.id) shouldBe Some(Journeys.limitedCompanyPages(1).id)

      refined.journeyPages.pages.map(_.id) shouldBe Journeys.limitedCompanyPages.map(_.id)
    }

    "Load journey with all pages saved" in {
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.businessRegistrationDetailsKey, FormTestData.someBpr)
        .withValue(Save4LaterKeys.businessTypeKey, BusinessType.SoleTrader)
        .withValue(Save4LaterKeys.verifiedEmailKey, ggEmail)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, 1529327782000L)
        .withValue(Page.contactPersonPage.id, FormTestData.contactPerson)
        .withValue(Page.mainBusinessAddressPage.id, FormTestData.mainBusinessAddress)
        .withValue(Page.nationalInsuranceNumberPage.id, FormTestData.nationalInsuranceNumber)
        .withValue(Page.tradingNamePage.id, FormTestData.tradingName)
        .withValue(Page.vatNumberPage.id, FormTestData.vatNumber)
        .withValue(Page.businessStatusPage.id, FormTestData.businessStatus)
        .withValue(Page.importingActivitiesPage.id, FormTestData.importingActivities)
        .withValue(Page.businessCustomersPage.id, FormTestData.businessCustomers)
        .withValue(Page.otherStoragePremisesPage.id, FormTestData.otherStoragePremises)
        .cacheMap
      setupSave4LaterFrom(cacheMap)

      val refined = refinedRequest(action, userRequest)

      refined.hasUpdates shouldBe None
      refined.lastUpdateTimestamp shouldBe 1529327782000L
      refined.journeyState.isComplete shouldBe true
      refined.journeyState.nextPageToComplete() shouldBe None
      refined.journeyState.lastEditedPage.map(_.id) shouldBe None

      refined.journeyPages.pages.map(_.id) shouldBe Journeys.soleTraderPages.map(_.id)
    }

    "Load journey for amendments with no change" in {
      val cacheMapBuilder = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.businessRegistrationDetailsKey, FormTestData.someBpr)
        .withValue(Save4LaterKeys.businessTypeKey, BusinessType.SoleTrader)
        .withValue(Save4LaterKeys.verifiedEmailKey, ggEmail)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, 1529327782000L)
        .withValue(Save4LaterKeys.journeyTypeKey, JourneyType.Amendment)

      val cacheMap = addUpdatePageData(cacheMapBuilder).cacheMap
      setupSave4LaterFrom(cacheMap)

      val refined = refinedRequest(action, userRequest)
      refined.hasUpdates shouldBe Some(false)
      refined.journeyType shouldBe JourneyType.Amendment

    }

    "Load journey for amendments with verified email change" in {
      val cacheMapBuilder = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.businessRegistrationDetailsKey, FormTestData.someBpr)
        .withValue(Save4LaterKeys.businessTypeKey, BusinessType.SoleTrader)
        .withValue(Save4LaterKeys.verifiedEmailKey, ggEmail)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, 1529327782000L)
        .withValue(Save4LaterKeys.journeyTypeKey, JourneyType.Amendment)

      val cacheMap = addUpdatePageData(cacheMapBuilder)
        .withValue(Save4LaterKeys.verifiedEmailKey, "other@tes.com")
        .cacheMap
      setupSave4LaterFrom(cacheMap)

      val refined = refinedRequest(action, userRequest)
      refined.hasUpdates shouldBe Some(true)
      refined.journeyType shouldBe JourneyType.Amendment

    }

    "Load journey for variations with contact person changes" in {
      val cacheMapBuilder = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.businessRegistrationDetailsKey, FormTestData.someBpr)
        .withValue(Save4LaterKeys.businessTypeKey, BusinessType.SoleTrader)
        .withValue(Save4LaterKeys.verifiedEmailKey, ggEmail)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, 1529327782000L)
        .withValue(Save4LaterKeys.journeyTypeKey, JourneyType.Variation)

      val cacheMap = addUpdatePageData(cacheMapBuilder)
        .withValue(Page.contactPersonPage.id, FormTestData.otherContactPerson)
        .cacheMap
      setupSave4LaterFrom(cacheMap)

      val refined = refinedRequest(action, userRequest)
      refined.hasUpdates shouldBe Some(true)
      refined.journeyType shouldBe JourneyType.Variation

    }
  }

  private def addUpdatePageData(cacheMapBuilder: CacheMapBuilder) = {
    cacheMapBuilder
      .withValue(Page.contactPersonPage.id, FormTestData.contactPerson)
      .withValue(Page.mainBusinessAddressPage.id, FormTestData.mainBusinessAddress)
      .withValue(Page.nationalInsuranceNumberPage.id, FormTestData.nationalInsuranceNumber)
      .withValue(Page.tradingNamePage.id, FormTestData.tradingName)
      .withValue(Page.vatNumberPage.id, FormTestData.vatNumber)
      .withValue(Page.businessStatusPage.id, FormTestData.businessStatus)
      .withValue(Page.importingActivitiesPage.id, FormTestData.importingActivities)
      .withValue(Page.businessCustomersPage.id, FormTestData.businessCustomers)
      .withValue(Page.otherStoragePremisesPage.id, FormTestData.otherStoragePremises)

      .withValue(Save4LaterKeys.displayKeyForPage(Page.contactPersonPage.id), FormTestData.contactPerson)
      .withValue(Save4LaterKeys.displayKeyForPage(Page.mainBusinessAddressPage.id), FormTestData.mainBusinessAddress)
      .withValue(Save4LaterKeys.displayKeyForPage(Page.nationalInsuranceNumberPage.id), FormTestData.nationalInsuranceNumber)
      .withValue(Save4LaterKeys.displayKeyForPage(Page.tradingNamePage.id), FormTestData.tradingName)
      .withValue(Save4LaterKeys.displayKeyForPage(Page.vatNumberPage.id), FormTestData.vatNumber)
      .withValue(Save4LaterKeys.displayKeyForPage(Page.businessStatusPage.id), FormTestData.businessStatus)
      .withValue(Save4LaterKeys.displayKeyForPage(Page.importingActivitiesPage.id), FormTestData.importingActivities)
      .withValue(Save4LaterKeys.displayKeyForPage(Page.businessCustomersPage.id), FormTestData.businessCustomers)
      .withValue(Save4LaterKeys.displayKeyForPage(Page.otherStoragePremisesPage.id), FormTestData.otherStoragePremises)

      .withValue(Save4LaterKeys.displayKeyForPage(Save4LaterKeys.verifiedEmailKey), ggEmail)

  }


}

