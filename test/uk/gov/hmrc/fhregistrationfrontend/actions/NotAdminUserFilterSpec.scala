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

package uk.gov.hmrc.fhregistrationfrontend.actions

import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{AffinityGroup, Assistant, User}
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{FhddsConnectorMocks, StubbedErrorHandler}

class NotAdminUserFilterSpec extends ActionSpecBase with FhddsConnectorMocks {

  implicit val errorHandler: StubbedErrorHandler.type = StubbedErrorHandler
  lazy val filter = new NotAdminUserFilter()(errorHandler, scala.concurrent.ExecutionContext.Implicits.global)

  "Not admin user filter" should {
    "allow user to proceed if has admin role" in {
      val request = new UserRequest("id", None, None, Some(User), Some(AffinityGroup.Individual), FakeRequest())
      status(result(filter, request)) shouldBe OK
    }

    "block the user and display forbidden if user has assistant role" in {
      val request = new UserRequest("id", None, None, Some(Assistant), Some(AffinityGroup.Individual), FakeRequest())
      status(result(filter, request)) shouldBe FORBIDDEN
    }

    "block the user and display bad request if user has no credential role" in {
      val request = new UserRequest("id", None, None, None, Some(AffinityGroup.Individual), FakeRequest())
      status(result(filter, request)) shouldBe BAD_REQUEST
    }
  }
}
