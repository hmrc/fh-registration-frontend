package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.DefaultWSCookie
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerTradingNameControllerISpec
  extends Specifications with TestConfiguration {

  val route = routes.BusinessPartnerTradingNameController.load().url.drop(6)

  s"GET $route" when {

    "render the business partner trading name page" when {
      "the user is authenticated" in {
        given.commonPrecondition
        val result = buildRequest(route)
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

  s"POST $route" when {

    "form with no errors" should {
      "redirect to the Business Partners National Insurance Number page" when {
        "the user is authenticated" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(
              xSessionId,
              "Csrf-Token" -> "nocheck"
            )
            .post(Map(
              "tradingName_yesNo" -> Seq("true"),
              "tradingName_value" -> Seq("Blue Peter")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnerNinoController.load().url)
          }
        }

        "the user selects no" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(
              xSessionId,
              "Csrf-Token" -> "nocheck"
            )
            .post(Map(
              "tradingName_yesNo" -> Seq("false"),
              "tradingName_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnerNinoController.load().url)
          }
        }
      }
    }

    "no radio option is selected by the user" should {
      "return 400" in {
        given.commonPrecondition

        val result = buildRequest(route)
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

    "yes is selected but no trading name is entered" should {
      "return 400" in {
        given.commonPrecondition

        val result = buildRequest(route)
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
