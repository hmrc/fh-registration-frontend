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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.DesToFormImpl
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, EmailVerificationConnectorMocks, FhddsConnectorMocks}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AmendmentControllerSpec
    extends ControllerSpecWithGuiceApp with BeforeAndAfterEach with ActionsMock with FhddsConnectorMocks
    with EmailVerificationConnectorMocks {

  val mockSave4LaterService = mock[Save4LaterService]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockActions,
      mockFhddsConnector,
      mockEmailVerifcationConnector,
      mockSave4LaterService
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
  )(using mockSave4LaterService, ec)

  "startAmendment" should {
    "Redirect to summary when an amendment is already in progress" in {
      setupStartAmendmentAction(Some(JourneyType.Amendment))

      val request = FakeRequest()
      val result = controller.startAmendment()(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }

    "Setup the save4Later store and redirect to summary" in {
      setupStartAmendmentAction(None)
      setupDesDisplayResult()
      setupEmailVerificationConnector("a@w.ro", true)

      when(mockSave4LaterService.saveBusinessRegistrationDetails(any(), any())(using any()))
        .thenReturn(Future.successful(None))
      when(mockSave4LaterService.saveBusinessType(any(), any())(using any()))
        .thenReturn(Future(Some(BusinessType.CorporateBody)))
      when(mockSave4LaterService.saveVerifiedEmail(any(), any())(using any()))
        .thenReturn(Future.successful(Some("a@w.ro")))

      for (page <- journeys.limitedCompanyPages) {
        when(mockSave4LaterService.saveDisplayData4Later(any(), any(), any())(using any(), any()))
          .thenReturn(Future.successful(None))
        when(mockSave4LaterService.saveDraftData4Later(any(), any(), any())(using any(), any()))
          .thenReturn(Future.successful(None))
      }

      when(mockSave4LaterService.saveDisplayDeclaration(any(), any())(using any())).thenReturn(Future.successful(None))
      when(mockSave4LaterService.saveJourneyType(any(), any())(using any()))
        .thenReturn(Future.successful(Some(JourneyType.Amendment)))

      val request = FakeRequest()
      val result = controller.startAmendment()(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }

    "Setup save4Later with unverified email" in {
      setupStartAmendmentAction(None)
      setupDesDisplayResult()
      setupEmailVerificationConnector("a@w.ro", false)
      when(mockSave4LaterService.saveBusinessRegistrationDetails(any(), any())(using any()))
        .thenReturn(Future.successful(None))
      when(mockSave4LaterService.saveBusinessType(any(), any())(using any()))
        .thenReturn(Future(Some(BusinessType.CorporateBody)))
      when(mockSave4LaterService.saveV1ContactEmail(any(), any())(using any()))
        .thenReturn(Future.successful(Some("a@w.ro")))

      for (page <- journeys.limitedCompanyPages) {
        when(mockSave4LaterService.saveDisplayData4Later(any(), any(), any())(using any(), any()))
          .thenReturn(Future.successful(None))
        when(mockSave4LaterService.saveDraftData4Later(any(), any(), any())(using any(), any()))
          .thenReturn(Future.successful(None))
      }

      when(mockSave4LaterService.saveDisplayDeclaration(any(), any())(using any())).thenReturn(Future.successful(None))
      when(mockSave4LaterService.saveJourneyType(any(), any())(using any()))
        .thenReturn(Future.successful(Some(JourneyType.Amendment)))

      val request = FakeRequest()
      val result = controller.startAmendment()(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }
  }

  "startVariation" should {
    "Redirect to summary when a variation is already in progress" in {
      setupStartVariationAction(Some(JourneyType.Variation))

      val request = FakeRequest()
      val result = controller.startVariation()(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }

    "Setup the save4Later store and redirect to summary" in {
      setupStartVariationAction(None)
      setupDesDisplayResult()
      setupEmailVerificationConnector("a@w.ro", true)
      when(mockSave4LaterService.saveBusinessRegistrationDetails(any(), any())(using any()))
        .thenReturn(Future.successful(None))
      when(mockSave4LaterService.saveBusinessType(any(), any())(using any()))
        .thenReturn(Future(Some(BusinessType.CorporateBody)))
      when(mockSave4LaterService.saveVerifiedEmail(any(), any())(using any()))
        .thenReturn(Future.successful(Some("a@w.ro")))

      for (page <- journeys.limitedCompanyPages) {
        when(mockSave4LaterService.saveDisplayData4Later(any(), any(), any())(using any(), any()))
          .thenReturn(Future.successful(None))
        when(mockSave4LaterService.saveDraftData4Later(any(), any(), any())(using any(), any()))
          .thenReturn(Future.successful(None))
      }

      when(mockSave4LaterService.saveDisplayDeclaration(any(), any())(using any())).thenReturn(Future.successful(None))
      when(mockSave4LaterService.saveJourneyType(any(), any())(using any()))
        .thenReturn(Future.successful(Some(JourneyType.Amendment)))

      val request = FakeRequest()
      val result = controller.startVariation()(request)
      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }
  }
}
