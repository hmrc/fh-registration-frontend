package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerCorporateBodyTradingNameControllerISpec
  extends Specifications with TestConfiguration {


  "GET /form/business-partners/corporate-body-trading-name" when {

    "render the business partner corporate body trading name page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/corporate-body-trading-name")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("Does the corporate body use a trading name that is different from its registered name?")
            page.getElementsByTag("h1").text must include("Does Shelby Limited use a trading name that is different from its registered name ?")
          }
        }
      }
    }

  }

  "POST /form/business-partners/corporate-body-trading-name" when {

    "the user selects yes and enters a trading name" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/form/business-partners/corporate-body-trading-name")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "tradingName_yesNo" -> Seq("true"),
                "tradingName_value" -> Seq("Shelby Company Limited")
              ))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Form submitted, with result: TradingName(true,Some(Shelby Company Limited))")
            }
          }
        }

        "the user selects no" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/form/business-partners/corporate-body-trading-name")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "tradingName_yesNo" -> Seq("false"),
                "tradingName_value" -> Seq.empty
              ))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Form submitted, with result: TradingName(false,None)")
            }
          }
        }
      }
    }

    "no radio option is selected by the user" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/corporate-body-trading-name")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "tradingName_yesNo" -> Seq.empty,
              "tradingName_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Does the corporate body use a trading name that is different from its registered name?")
            page.getElementsByTag("h1").text() must include("Does Shelby Limited use a trading name that is different from its registered name ?")
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Select whether the business has a different trading name")
          }
        }
      }
    }

    "yes is selected but no trading name is entered" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/corporate-body-trading-name")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "tradingName_yesNo" -> Seq("true"),
              "tradingName_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Does the corporate body use a trading name that is different from its registered name?")
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the trading name")
          }
        }
      }
    }


  }

}