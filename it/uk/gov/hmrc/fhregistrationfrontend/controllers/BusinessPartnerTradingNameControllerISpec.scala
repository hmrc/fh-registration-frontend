package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerTradingNameControllerISpec
  extends Specifications with TestConfiguration {


  "GET /form/business-partners/partner-trading-name" when {

    "render the business partner trading name page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partner-trading-name")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
             val page = Jsoup.parse(res.body)
             page.title must include("Does the partner’s business use a trading name that is different from its registered name?")
             page.getElementsByTag("h1").text must include("Does Test User’s business use a trading name that is different from its registered name?")
          }
        }
      }
    }

  }

  "POST /form/business-partners/partner-trading-name" when {

    "form with no errors" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/form/business-partners/partner-trading-name")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "tradingName_yesNo" -> Seq("true"),
                "tradingName_value" -> Seq("Blue Peter")
              ))

            whenReady(result) { res =>
              res.status mustBe 200
            }
          }
        }

        "the user selects no" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/form/business-partners/partner-trading-name")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "tradingName_yesNo" -> Seq("false"),
                "tradingName_value" -> Seq.empty
              ))

            whenReady(result) { res =>
              res.status mustBe 200
            }
          }
        }
      }
    }

    "no radio option is selected by the user" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partner-trading-name")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "tradingName_yesNo" -> Seq.empty,
              "tradingName_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
          }
        }
      }
    }

    "yes is selected but no trading name is entered" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partner-trading-name")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "tradingName_yesNo" -> Seq("true"),
              "tradingName_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
          }
        }
      }
    }


  }

}
