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

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.api.libs.ws.DefaultBodyWritables.writeableOf_urlEncodedForm
import play.api.libs.ws.DefaultBodyReadables.*

class OtherStoragePremisesControllerISpec extends Specifications with TestConfiguration {
  val requestUrl = "otherStoragePremises"

  "GET /otherStoragePremises" when {

    "render the Other Storage Premises page" when {
      "the user is authenticated" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(
              "Does the business use any UK premises to store third-party goods imported from outside the UK?"
            )
          }
        }
      }
    }

  }

  "POST /otherStoragePremises" when {

    "Yes radio button selected" should {
      "return 200" when {
        "the user is authenticated" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "otherStoragePremises" -> Seq("true")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 200
              res.body[String] must include("Form submitted, with result: true")
            }
          }
        }

        "No radio button is selected" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "otherStoragePremises" -> Seq("false")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 200
              res.body[String] must include("Form submitted, with result: false")
            }
          }
        }
      }
    }

    "a radio button isn't selected" should {
      "return 400" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(
              Map(
                "otherStoragePremises" -> Seq.empty
              )
            )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include(
              "Does the business use any UK premises to store third-party goods imported from outside the UK?"
            )
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
              "Select whether the business uses other premises for storing non-UK goods"
            )
          }
        }
      }
    }
  }

}
