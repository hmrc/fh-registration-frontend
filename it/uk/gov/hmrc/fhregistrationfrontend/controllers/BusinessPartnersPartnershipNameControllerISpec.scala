package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersPartnershipNameControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersPartnershipNameController.load().url.drop(6)
  val pageHeading: String = "What is the name of the partnership?"
  val pageTitle: String = "What is the name of the partnership? - Business partners"
  val emptyTextError: String = "Enter a partnership name"
  val characterLimitError: String = "Partnership name must be 120 characters or less"
  val tradingNamePageUrl: String = routes.BusinessPartnersPartnershipTradingNameController.load().url

  s"GET $route" when {

    "render the business partners partnership name page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        val result = buildRequest(route).addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(pageTitle)
          page.getElementsByTag("h1").text must include(pageHeading)
        }
      }
    }

  }

  s"POST $route" when {

    "the user submits with a company name" should {
      "return 303" when {
        "the user is authenticated" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "partnershipName" -> Seq("Shelby Limited")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(tradingNamePageUrl)
          }
        }
      }
    }

    "User does not enter a partnership name" should {
      "return 400" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(
            "partnershipName" -> Seq("")
          ))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.title must include(pageTitle)
          page.getElementsByTag("h1").text() must include(pageHeading)
          page.getElementById("partnershipName-error").text() must include(emptyTextError)
        }
      }
    }

    "User enters over 120 characters" should {
      "return 400" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(
            "partnershipName" -> Seq("hghghghghghghghhghghghghghghghhghghgh" +
              "ghghghghhghghghghghghghhghghghghghghghhghghghghghghghhghghgh" +
              "ghghghghhghghghghghghghhghghghghghghghhghghghghghghghhghghghg")
          ))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.title must include(pageTitle)
          page.getElementsByTag("h1").text() must include(pageHeading)
          page.getElementById("partnershipName-error").text() must include(characterLimitError)
        }
      }
    }
  }
}