package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Helpers.redirectLocation
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerUnincorporatedBodyTradingNameControllerISpec
  extends Specifications with TestConfiguration {


  "GET /form/business-partners/unincorporated-body-trading-name" when {

    "render the business partner unincorporated body trading name page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/unincorporated-body-trading-name")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("Does the unincorporated body use a trading name that is different from its registered name?")
            page.getElementsByTag("h1").text must include("Does Shelby unincorporated use a trading name that is different from its registered name?")
          }
        }
      }
    }

  }

  "POST /form/business-partners/unincorporated-body-trading-name" should {

    "redirect to the 'does the unincorporated body have a uk vat reg number' page" when {
      "the user selects yes and enters a trading name" in {
        given.commonPrecondition


        val result = buildRequest("/form/business-partners/unincorporated-body-trading-name")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(
            "tradingName_yesNo" -> Seq("true"),
            "tradingName_value" -> Seq("Shelby Unincorporated")
          ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/unincorporated-body-vat-registration-number")
        }
      }

      "the user selects no" in {
        given.commonPrecondition

        val result = buildRequest("/form/business-partners/unincorporated-body-trading-name")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(
            "tradingName_yesNo" -> Seq("false"),
            "tradingName_value" -> Seq.empty
          ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/unincorporated-body-vat-registration-number")
        }
      }
    }

    "return 400" when {
      "no radio option is selected by the user" in {
        given.commonPrecondition

        val result = buildRequest("/form/business-partners/unincorporated-body-trading-name")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(
            "tradingName_yesNo" -> Seq.empty,
            "tradingName_value" -> Seq.empty
          ))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.title must include("Does the unincorporated body use a trading name that is different from its registered name?")
          page.getElementsByTag("h1").text() must include("Does Shelby unincorporated use a trading name that is different from its registered name?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Select whether the business has a different trading name")
        }
      }

      "yes is selected but no trading name is entered" in {
        given.commonPrecondition

        val result = buildRequest("/form/business-partners/unincorporated-body-trading-name")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(
            "tradingName_yesNo" -> Seq("true"),
            "tradingName_value" -> Seq.empty
          ))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.title must include("Does the unincorporated body use a trading name that is different from its registered name?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the trading name")
        }
      }
    }

  }
}
