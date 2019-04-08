/*
 * Copyright 2019 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.{any, same}
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{Admin, AffinityGroup, Assistant}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterKeys
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{CacheMapBuilder, FhddsConnectorMocks, Save4LaterMocks, StubbedErrorHandler}

class StartAmendmentActionSpec
  extends ActionSpecBase
    with Save4LaterMocks
    with FhddsConnectorMocks {


  val errorHandler = StubbedErrorHandler

  lazy val action = new StartAmendmentAction(mockFhddsConnector)(mockSave4Later, errorHandler)

  "Start amendment action " should {
    "Fail when no fhdds registration number" in {
      val userRequest = new UserRequest(testUserId, None, registrationNumber = None, None, Some(AffinityGroup.Individual), FakeRequest())

      status(result(action, userRequest)) shouldBe BAD_REQUEST
    }

    "Find the correct journey type" in {
      val userRequest = new UserRequest(testUserId, None, Some(registrationNumber), None, Some(AffinityGroup.Individual), FakeRequest())

      setupFhddsStatus(FhddsStatus.Received)
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.journeyTypeKey, JourneyType.Amendment)
        .cacheMap

      setupSave4LaterFrom(cacheMap)
      val refined = refinedRequest(action, userRequest)

      refined.currentJourneyType shouldBe Some(JourneyType.Amendment)
    }

    "Fail for some statuses" in {
      import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus._
      val userRequest = new UserRequest(testUserId, None, Some(registrationNumber), Some(Admin), Some(AffinityGroup.Individual), FakeRequest())
      for {
        fhddsStatus ← List(Approved, ApprovedWithConditions, Rejected, Revoked, Withdrawn, Deregistered)
      } {
        val fhddsConnector = mock[FhddsConnector]
        when(fhddsConnector.getStatus(same(registrationNumber))(any())) thenReturn fhddsStatus

        val action = new StartAmendmentAction(fhddsConnector)(mockSave4Later, errorHandler)
        status(result(action, userRequest)) shouldBe BAD_REQUEST

      }
    }

    "Work for some statuses" in {
      import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus._
      val userRequest = new UserRequest(testUserId, None, Some(registrationNumber), Some(Assistant), Some(AffinityGroup.Individual), FakeRequest())
      for {
        fhddsStatus ← List(Received, Processing)
      } {
        val fhddsConnector = mock[FhddsConnector]
        when(fhddsConnector.getStatus(same(registrationNumber))(any())) thenReturn fhddsStatus
        val action = new StartAmendmentAction(fhddsConnector)(mockSave4Later, errorHandler)

        setupSave4LaterFrom(CacheMapBuilder(testUserId).cacheMap)

        val refined = refinedRequest(action, userRequest)
        refined.registrationNumber shouldBe registrationNumber
        refined.userId shouldBe testUserId
        refined.currentJourneyType shouldBe Some(JourneyType.New)
      }
    }

  }

}
