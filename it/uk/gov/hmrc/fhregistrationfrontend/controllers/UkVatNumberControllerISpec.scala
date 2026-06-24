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
import play.api.libs.ws.DefaultBodyReadables.readableAsString

class UkVatNumberControllerISpec extends Specifications with TestConfiguration {

  "GET /form/business-partners/corporate-body-vat-registration-number" should {

    "render the corporate-body-vat-registration-number page" in {
      `given`.commonPrecondition

      WsTestClient.withClient { client =>
        val result = client
          .url(s"$baseUrl/vatNumber ")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("Does the business have a UK VAT registration number?")
          page.getElementsByTag("h1").text() must include("Does the business have a UK VAT registration number?")
        }
      }
    }
  }

  "POST /form/business-partners/corporate-body-vat-registration-number" when {
    "yes is selected and the vatnumber entered" should {
      "return 200 with vatnumber" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/vatNumber ")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("vatNumber_yesNo" -> Seq("true"), "vatNumber_value" -> Seq("123456789")))

          whenReady(result) { res =>
            res.status mustBe 200
            res.body mustBe "Next page!"
          }
        }
      }
    }

    "no is selected" should {
      "return 200 with no vatnumber message" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/vatNumber")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("vatNumber_yesNo" -> Seq("false")))

          whenReady(result) { res =>
            res.status mustBe 200
            res.body mustBe "Next page!"
          }
        }
      }
    }

    "no hasVat number selected" should {
      "return 400" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/vatNumber")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("vatNumber_yesNo" -> Seq.empty))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include(
              "There is a problem Select whether the business has a VAT registration number"
            )
          }
        }
      }
    }

    "hasVat number selected but vatNumber not supplied" should {
      "return 400" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/vatNumber")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("vatNumber_yesNo" -> Seq("true")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include(
              "There is a problem Enter the VAT registration number"
            )
          }
        }
      }
    }

    "the form hasVat field is invalid format" should {
      "return 400" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/vatNumber")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("vatNumber_yesNo" -> Seq("error")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include(
              "There is a problem Select whether the business has a VAT registration number"
            )
          }
        }
      }
    }

    "the vatnumber field is invalid format" should {
      "return 400" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/vatNumber")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("vatNumber_yesNo" -> Seq("true"), "vatNumber_value" -> Seq("1234")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include(
              "There is a problem Enter a valid UK VAT registration number"
            )
          }
        }
      }
    }
  }
}
