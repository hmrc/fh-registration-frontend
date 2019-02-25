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
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.ContactPerson
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterKeys
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{CacheMapBuilder, Save4LaterMocks, StubbedErrorHandler}

class EmailVerificationActionSpec extends ActionSpecBase with Save4LaterMocks {


  lazy val action = new EmailVerificationAction()(mockSave4Later, StubbedErrorHandler)
  val verifiedEmail = "verified@test.com"
  val contactEmail = "contact@test.com"
  val pendingEmail = "pending@test.com"


  "EmailVerificationAction" should {
    "Find the verified email" in {
      val cacheMap =
        CacheMapBuilder(testUserId)
          .withValue(Save4LaterKeys.verifiedEmailKey, verifiedEmail)
          .cacheMap

      setupSave4LaterFrom(cacheMap)

      val userRequest = new UserRequest(testUserId, None, None, FakeRequest())

      val refined = refinedRequest(action, userRequest)
      refined.verifiedEmail shouldBe Some(verifiedEmail)
      refined.pendingEmail shouldBe None
      refined.candidateEmail shouldBe Some(verifiedEmail)
    }

    "Find the from contact person" in {
      val contactPerson = ContactPerson(
        "f", "l", "job", "1231231", Some(contactEmail), true, None, None, None)

      val cacheMap =
        CacheMapBuilder(testUserId)
          .withValue("contactPerson", contactPerson)
          .cacheMap

      setupSave4LaterFrom(cacheMap)

      val userRequest = new UserRequest(testUserId, None, None, FakeRequest())

      val refined = refinedRequest(action, userRequest)
      refined.verifiedEmail shouldBe None
      refined.pendingEmail shouldBe None
      refined.candidateEmail shouldBe Some(contactEmail)
    }

    "Find the from contact person v1" in {

      val cacheMap =
        CacheMapBuilder(testUserId)
          .withValue(Save4LaterKeys.v1ContactEmailKey, contactEmail)
          .cacheMap

      setupSave4LaterFrom(cacheMap)

      val userRequest = new UserRequest(testUserId, None, None, FakeRequest())

      val refined = refinedRequest(action, userRequest)
      refined.verifiedEmail shouldBe None
      refined.candidateEmail shouldBe Some(contactEmail)
    }


    "Find the pending email" in {

      val cacheMap =
        CacheMapBuilder(testUserId)
          .withValue(Save4LaterKeys.pendingEmailKey, pendingEmail)
          .cacheMap

      setupSave4LaterFrom(cacheMap)

      val userRequest = new UserRequest(testUserId, None, None, FakeRequest())

      val refined = refinedRequest(action, userRequest)
      refined.verifiedEmail shouldBe None
      refined.pendingEmail shouldBe Some(pendingEmail)
      refined.candidateEmail shouldBe Some(pendingEmail)
    }


    "Find the gg email" in {

      val cacheMap = CacheMapBuilder(testUserId).cacheMap

      setupSave4LaterFrom(cacheMap)

      val userRequest = new UserRequest(testUserId, Some(ggEmail), None, FakeRequest())

      val refined = refinedRequest(action, userRequest)
      refined.verifiedEmail shouldBe None
      refined.pendingEmail shouldBe None
      refined.candidateEmail shouldBe Some(ggEmail)
    }

    "Have correct user id " in {
      val cacheMap = CacheMapBuilder(testUserId).cacheMap
      setupSave4LaterFrom(cacheMap)
      val userRequest = new UserRequest(testUserId, Some(ggEmail), None, FakeRequest())

      val refined = refinedRequest(action, userRequest)

      refined.userId shouldBe testUserId
    }
  }


}
