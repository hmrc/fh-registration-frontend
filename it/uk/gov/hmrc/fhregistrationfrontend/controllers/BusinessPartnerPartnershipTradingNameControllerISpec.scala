package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerPartnershipTradingNameControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnerPartnershipTradingNameController.load().url.drop(6)
  val partnershipVatRegNumUrl: String = routes.BusinessPartnersPartnershipVatNumberController.load().url
  val partnershipCompanyRegNumUrl: String = routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.load().url

  s"GET $route" when {

    "render the business partner partnership trading name page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include("Does the partnership use a trading name that is different from its registered name?")
          page.getElementsByTag("h1").text must include("Does Test User use a trading name that is different from its registered name?")
        }
      }
    }

  }

  s"POST $route" when {
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
          page.title must include("Does the partnership use a trading name that is different from its registered name?")
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
          page.title must include("Does the partnership use a trading name that is different from its registered name?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the trading name")
        }
      }
    }


    "the businessType/legal entity of the partnership is a 'partnership'" when {
      "the user selects no" should {
        "redirect to the partnership name page" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "partnership")
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "tradingName_yesNo" -> Seq("false"),
              "tradingName_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(partnershipVatRegNumUrl)
          }
        }
      }

      "the user selects yes and enters a trading name" should {
        "redirect to the partnership name page" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "partnership")
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "tradingName_yesNo" -> Seq("true"),
              "tradingName_value" -> Seq("Shelby Company Limited")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(partnershipVatRegNumUrl)
          }
        }
      }
    }

    "the businessType/legal entity of the partnership is a 'limited liability partnership'" when {
      "the user selects no" should {
      "redirect to the partnership name page" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "limited-liability-partnership")
          )
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(
            "tradingName_yesNo" -> Seq("false"),
            "tradingName_value" -> Seq.empty
          ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(partnershipCompanyRegNumUrl)
        }
      }
    }

      "the user selects yes and enters a trading name" should {
      "redirect to the partnership name page" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "limited-liability-partnership")
          )
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(
            "tradingName_yesNo" -> Seq("true"),
            "tradingName_value" -> Seq("Shelby Company Limited")
          ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(partnershipCompanyRegNumUrl)
          }
        }
      }
    }

  }

}