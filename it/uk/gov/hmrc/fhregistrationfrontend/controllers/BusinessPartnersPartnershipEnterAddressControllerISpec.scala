package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.Helpers.contentAsString
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersPartnershipEnterAddressControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersPartnershipEnterAddressController.load().url.drop(6)
  val checkYouAnswersPage: String = routes.BusinessPartnersCheckYourAnswersController.load().url

  s"GET $route" when {

    "the new business partners flow is enabled" should {

      "render the business partner enter partnership registration office address page" when {
        "the user is authenticated" in {
          given
            .commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include("Enter the partnership’s registered office address?")
            page.getElementById("page-heading").text() must include("Enter")
            page.getElementById("page-heading").text() must include("registered office address?")
          }
        }
      }
    }
  }

  s"POST $route" when {
    "the new business partners flow is enabled" should {
      "redirect to the Check Your Answers page" when {
        "only mandatory fields are populated" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "enterAddress.line1" -> Seq("1 Street"),
              "enterAddress.line2" -> Seq.empty,
              "enterAddress.line3" -> Seq("City name"),
              "enterAddress.postcode" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(checkYouAnswersPage)
          }
        }

        "all fields are populated" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "enterAddress.line1" -> Seq("1 street"),
              "enterAddress.line2" -> Seq("Option lane"),
              "enterAddress.line3" -> Seq("City name"),
              "enterAddress.postcode" -> Seq("AB1 2XZ")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(checkYouAnswersPage)
          }
        }
      }

      "return a 400 error" when {
        "mandatory fields are not populated" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "enterAddress.line1" -> Seq.empty,
              "enterAddress.line2" -> Seq.empty,
              "enterAddress.line3" -> Seq.empty,
              "enterAddress.postcode" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
              "You must enter line 1 of the address")
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
              "You must enter the Town or City of the address")
          }
        }
      }
    }
  }
}
