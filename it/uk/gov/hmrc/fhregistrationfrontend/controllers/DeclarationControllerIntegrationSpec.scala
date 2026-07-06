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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.api.libs.ws.DefaultBodyWritables.writeableOf_urlEncodedForm

class DeclarationControllerIntegrationSpec extends Specifications with TestConfiguration {

  "DeclarationController" should {

    "Show the declaration page when the user has fulfilled all the pages" in {

      `given`.summaryPrecondition

      WsTestClient.withClient { client =>
        val result = client
          .url(s"$baseUrl/declaration")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the declaration form" in {
      `given`.summaryPrecondition.fhddsBackend.createSubscription()

      WsTestClient.withClient { client =>
        val result =
          client
            .url(s"$baseUrl/submit")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "some-id", "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(
              Map(
                "fullName"          -> Seq("Tester"),
                "jobTitle"          -> Seq("Dev"),
                "usingDefaultEmail" -> Seq("true"),
                "defaultEmail"      -> Seq("user@test.com")
              )
            )

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/acknowledgement")
        }

      }

    }

  }

}
