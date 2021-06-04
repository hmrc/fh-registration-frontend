/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import play.api.libs.json.JsValue
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.DesToFormImpl
import uk.gov.hmrc.fhregistrationfrontend.services.{Save4LaterKeys, Save4LaterService}
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, EmailVerificationConnectorMocks, FhddsConnectorMocks, InMemoryShortLivedCache}
import uk.gov.hmrc.http.HeaderCarrier

class AmendmentControllerSpec
    extends ControllerSpecWithGuiceApp with BeforeAndAfterEach with ActionsMock with FhddsConnectorMocks
    with EmailVerificationConnectorMocks {

  val inMemorySave4Later = new Save4LaterService(new InMemoryShortLivedCache(testUserId))

  override def beforeEach(): Unit = {
    super.beforeEach()
    inMemorySave4Later.removeUserData(testUserId)(HeaderCarrier())
    reset(
      mockActions,
      mockFhddsConnector,
      mockEmailVerifcationConnector
    )
  }

  val controller = new AmendmentController(
    new DesToFormImpl,
    commonDependencies,
    mockFhddsConnector,
    mockEmailVerifcationConnector,
    mockMcc,
    mockActions,
    journeys
  )(inMemorySave4Later, scala.concurrent.ExecutionContext.Implicits.global)

  "startAmendment" should {
    "Redirect to summary when an amendment is already in progress" in {
      setupStartAmendmentAction(Some(JourneyType.Amendment))

      val request = FakeRequest()
      val result = await(controller.startAmendment()(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }

    "Setup the save4Later store and redirect to summary" in {
      setupStartAmendmentAction(None)
      setupDesDisplayResult()
      setupEmailVerificationConnector("a@w.ro", true)
      implicit val hc = HeaderCarrier()

      val request = FakeRequest()
      val result = await(controller.startAmendment()(request))

      await(inMemorySave4Later.fetchBusinessRegistrationDetails(testUserId)) shouldBe defined
      await(inMemorySave4Later.fetchBusinessType(testUserId)) shouldBe Some(BusinessType.CorporateBody.toString)
      await(inMemorySave4Later.fetchVerifiedEmail(testUserId)) shouldBe Some("a@w.ro")

      for (page ← journeys.limitedCompanyPages) {
        await(inMemorySave4Later.fetchData4Later[JsValue](testUserId, page.id)) shouldBe defined
        await(inMemorySave4Later.fetchData4Later[JsValue](testUserId, Save4LaterKeys.displayKeyForPage(page.id))) shouldBe defined
      }

      await(inMemorySave4Later.fetchData4Later[JourneyType](testUserId, Save4LaterKeys.journeyTypeKey)) shouldBe Some(
        JourneyType.Amendment)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }

    "Setup save4Later with unverified email" in {
      setupStartAmendmentAction(None)
      setupDesDisplayResult()
      setupEmailVerificationConnector("a@w.ro", false)
      implicit val hc = HeaderCarrier()

      val request = FakeRequest()
      val result = await(controller.startAmendment()(request))

      await(inMemorySave4Later.fetchVerifiedEmail(testUserId)) shouldBe None
      await(inMemorySave4Later.fetchV1ContactEmail(testUserId)) shouldBe Some("a@w.ro")

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }
  }

  "startVariation" should {
    "Redirect to summary when a variation is already in progress" in {
      setupStartVariationAction(Some(JourneyType.Variation))

      val request = FakeRequest()
      val result = await(controller.startVariation()(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }

    "Setup the save4Later store and redirect to summary" in {
      setupStartVariationAction(None)
      setupDesDisplayResult()
      setupEmailVerificationConnector("a@w.ro", true)
      implicit val hc = HeaderCarrier()

      val request = FakeRequest()
      val result = await(controller.startVariation()(request))

      await(inMemorySave4Later.fetchBusinessRegistrationDetails(testUserId)) shouldBe defined
      await(inMemorySave4Later.fetchBusinessType(testUserId)) shouldBe Some(BusinessType.CorporateBody.toString)
      await(inMemorySave4Later.fetchVerifiedEmail(testUserId)) shouldBe Some("a@w.ro")

      for (page ← journeys.limitedCompanyPages) {
        await(inMemorySave4Later.fetchData4Later[JsValue](testUserId, page.id)) shouldBe defined
        await(inMemorySave4Later.fetchData4Later[JsValue](testUserId, Save4LaterKeys.displayKeyForPage(page.id))) shouldBe defined
      }

      await(inMemorySave4Later.fetchData4Later[JourneyType](testUserId, Save4LaterKeys.journeyTypeKey)) shouldBe Some(
        JourneyType.Amendment)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }
  }
}
