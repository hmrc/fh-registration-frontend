/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.emailverification

import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.api.libs.ws.writeableOf_urlEncodedForm

class AmendWithVerifiedEmailSpec extends Specifications with TestConfiguration {

  "Submitting an amendment" should {
    "Allow submission" when {
      "The verified email was amended" in {

        `given`.user.isAuthorisedAndEnrolled.save4later
          .acceptsDelete()
          .audit
          .writesAuditOrMerged()
          .fhddsBackend
          .acceptsAmendments()
          .save4later
          .hasAmendmentDataWithNewVerifiedEmail("a@test.com")

        WsTestClient.withClient { implicit client =>
          val result = client
            .url(s"$baseUrl/submit")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "some-id", "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(
              Map(
                "fullName"          -> Seq("Tester"),
                "jobTitle"          -> Seq("Dev"),
                "usingDefaultEmail" -> Seq("true"),
                "defaultEmail"      -> Seq("a@test.com")
              )
            )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header("Location") mustBe Some("/fhdds/acknowledgement")

            expect.fhddsBackend.amendWasCalled.fhddsBackend
              .contactEmailMatches("a@test.com")
              .fhddsBackend
              .contactDetailChangedFlag(true)

          }
        }
      }
    }
  }
}
