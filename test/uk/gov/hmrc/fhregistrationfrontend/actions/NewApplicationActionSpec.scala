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
import uk.gov.hmrc.auth.core.{Admin, AffinityGroup, Assistant}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus._
import uk.gov.hmrc.fhregistrationfrontend.teststubs.StubbedErrorHandler

class NewApplicationActionSpec extends ActionSpecBase {

  "New Application Action" should {
    "Be denied for some statuses" in {
      val userRequest = new UserRequest(testUserId, None, Some(registrationNumber), Some(Admin), Some(AffinityGroup.Individual), FakeRequest())
      for {
        fhddsStatus ← List(Processing, Received, Approved, ApprovedWithConditions)
      } {
        val fhddsConnector = mock[FhddsConnector]
        when(fhddsConnector.getStatus(same(registrationNumber))(any())) thenReturn fhddsStatus
        val action = new NewApplicationAction(fhddsConnector)(StubbedErrorHandler, scala.concurrent.ExecutionContext.Implicits.global)

        status(result(action, userRequest)) shouldBe BAD_REQUEST
      }
    }

    "Be allowed when there is no registration number" in {
      val userRequest = new UserRequest(testUserId, None, registrationNumber = None, Some(Admin), Some(AffinityGroup.Individual), FakeRequest())
      val fhddsConnector = mock[FhddsConnector]
      val action = new NewApplicationAction(fhddsConnector)(StubbedErrorHandler, scala.concurrent.ExecutionContext.Implicits.global)

      status(result(action, userRequest)) shouldBe OK
    }

    "Be allowed for some statuses" in {
      val userRequest = new UserRequest(testUserId, None, Some(registrationNumber), Some(Admin), Some(AffinityGroup.Individual), FakeRequest())
      for {
        fhddsStatus ← List(Rejected, Revoked, Withdrawn, Deregistered)
      } {
        val fhddsConnector = mock[FhddsConnector]
        when(fhddsConnector.getStatus(same(registrationNumber))(any())) thenReturn fhddsStatus
        val action = new NewApplicationAction(fhddsConnector)(StubbedErrorHandler, scala.concurrent.ExecutionContext.Implicits.global)

        status(result(action, userRequest)) shouldBe OK
      }
    }
  }
}
