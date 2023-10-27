package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.mvc.Http.HeaderNames

class BusinessPartnersCorporateBodyTradingNameControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersCorporateBodyTradingNameController.load().url.drop(6)
  val companyRegNumUrl: String = routes.BusinessPartnersCorporateBodyCompanyRegistrationNumberController.load().url

  s"GET $route" when {

    "render the business partner corporate body trading name page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include("Does the corporate body use a trading name that is different from its registered name?")
          page.getElementsByTag("h1").text must include("Does Shelby Limited use a trading name that is different from its registered name?")
        }
      }
    }

  }

  s"POST $route" when {

    "the user selects yes and enters a trading name" should {
      "return 303" when {
        "the user is authenticated" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck").withFollowRedirects(false)
            .post(Map(
              "tradingName_yesNo" -> Seq("true"),
              "tradingName_value" -> Seq("Shelby Company Limited")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(companyRegNumUrl)
          }
        }

        "the user selects no" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck").withFollowRedirects(false)
            .post(Map(
              "tradingName_yesNo" -> Seq("false"),
              "tradingName_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(companyRegNumUrl)
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
          val page = Jsoup.parse(res.body)
          page.title must include("Does the corporate body use a trading name that is different from its registered name?")
          page.getElementsByTag("h1").text() must include("Does Shelby Limited use a trading name that is different from its registered name?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Select whether the business has a different trading name")
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
          val page = Jsoup.parse(res.body)
          page.title must include("Does the corporate body use a trading name that is different from its registered name?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the trading name")
        }
      }
    }
  }
}