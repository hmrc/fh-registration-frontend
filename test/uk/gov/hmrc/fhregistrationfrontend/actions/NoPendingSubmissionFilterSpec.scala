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

import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.{AffinityGroup, Assistant}
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{FhddsConnectorMocks, StubbedErrorHandler}

class NoPendingSubmissionFilterSpec extends ActionSpecBase with FhddsConnectorMocks {

  val request = new UserRequest("id", None, None, Some(Assistant), Some(AffinityGroup.Individual), FakeRequest())
  lazy val filter = new NoPendingSubmissionFilter(mockFhddsConnector)(StubbedErrorHandler, scala.concurrent.ExecutionContext.Implicits.global)

  "No pending submission filter" should {
    "allow user to proceed" in {
      setupFhddsEnrolmentProgress(EnrolmentProgress.Unknown)
      status(result(filter, request)) shouldBe OK

    }

    "block the user" in {
      setupFhddsEnrolmentProgress(EnrolmentProgress.Pending)
      status(result(filter, request)) shouldBe BAD_REQUEST
    }
  }
}
