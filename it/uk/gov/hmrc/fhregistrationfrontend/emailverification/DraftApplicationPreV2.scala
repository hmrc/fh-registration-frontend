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

class DraftApplicationPreV2 extends Specifications with TestConfiguration {

  "Loading a draft application pre2.0" should {
    "redirect the user to email-verification-status" when {
      "the user loads the summary page" in {

        `given`.user.isAuthorised().save4later.hasFullPreEmailVerificationData().audit.writesAuditOrMerged()

        WsTestClient withClient { implicit client =>
          val result = client
            .url(s"$baseUrl/summary")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header("Location") mustBe Some("/fhdds/email-verification-status")
          }
        }
      }

      "the user loads a form page" in {
        `given`.user.isAuthorised().save4later.hasFullPreEmailVerificationData().audit.writesAuditOrMerged()

        WsTestClient withClient { implicit client =>
          val result = client
            .url(s"$baseUrl/form/mainBusinessAddressPage")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header("Location") mustBe Some("/fhdds/email-verification-status")
          }
        }
      }

      "the user loads the declaration page" in {
        `given`.user.isAuthorised().save4later.hasFullPreEmailVerificationData().audit.writesAuditOrMerged()

        WsTestClient withClient { implicit client =>
          val result = client
            .url(s"$baseUrl/declaration")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header("Location") mustBe Some("/fhdds/email-verification-status")
          }
        }
      }
    }
  }
}
