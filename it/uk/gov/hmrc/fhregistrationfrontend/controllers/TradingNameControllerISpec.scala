package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.api.libs.ws.DefaultBodyWritables.writeableOf_urlEncodedForm
import play.api.libs.ws.DefaultBodyReadables.*

class TradingNameControllerISpec extends Specifications with TestConfiguration {

  val requestUrl = "tradingName"

  "GET /tradingName" when {

    "render the trading name page" when {
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
            page.title must include("Trading name - Apply for the Fulfilment House Due Diligence Scheme")
            page.getElementsByTag("h1").text must include(
              "Does the business use a trading name that is different from its registered one?"
            )
          }
        }
      }
    }

  }

  "POST /tradingName" when {

    "the user selects yes and enters a trading name" should {
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
                  "tradingName_yesNo" -> Seq("true"),
                  "tradingName_value" -> Seq("Shelby Company Limited")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 200
              res.body[String] must include(
                "Form submitted, with result: TradingName(true,Some(Shelby Company Limited))"
              )
            }
          }
        }

        "the user selects no" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "tradingName_yesNo" -> Seq("false"),
                  "tradingName_value" -> Seq.empty
                )
              )

            whenReady(result) { res =>
              res.status mustBe 200
              res.body[String] must include("Form submitted, with result: TradingName(false,None)")
            }
          }
        }
      }
    }

    "no radio option is selected by the user" should {
      "return 400" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(
              Map(
                "tradingName_yesNo" -> Seq.empty,
                "tradingName_value" -> Seq.empty
              )
            )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Trading name - Apply for the Fulfilment House Due Diligence Scheme")
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
              "Select whether the business has a different trading name"
            )
          }
        }
      }
    }

    "yes is selected but no trading name is entered" should {
      "return 400" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(
              Map(
                "tradingName_yesNo" -> Seq("true"),
                "tradingName_value" -> Seq.empty
              )
            )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Trading name - Apply for the Fulfilment House Due Diligence Scheme")
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
              "Enter the trading name"
            )
          }
        }
      }
    }
  }

}
