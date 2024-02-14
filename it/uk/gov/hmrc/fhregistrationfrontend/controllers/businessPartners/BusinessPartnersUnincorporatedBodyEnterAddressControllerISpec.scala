package uk.gov.hmrc.fhregistrationfrontend.controllers.businessPartners

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersUnincorporatedBodyEnterAddressControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersUnincorporatedBodyEnterAddressController.load().url.drop(6)
  val checkYouAnswersPage: String = routes.BusinessPartnersCheckYourAnswersController.load().url
  val pageTitle = "Enter the company’s registered office address?"
  val pageHeading = "Enter Test Unincorporated Body’s registered office address"

  s"GET $route" when {

    "the new business partners flow is enabled" should {

      "render the enter unincorporated body registered office address page" when {
        "the user is authenticated" in {
          given
            .commonPrecondition

            val result = buildRequest(route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include(pageTitle)
              page.getElementsByTag("h1").text() must include(pageHeading)
            }
        }
      }
    }
  }

  s"POST $route" when {

    "the new business partners flow is enabled" should {
      "redirect when form is filled out correctly with all fields populated" when {
        "the user is authenticated" in {
          given
            .commonPrecondition

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

        "redirect when form is filled out correctly with only mandatory fields populated" when {
          "the user is authenticated" in {
            given
              .commonPrecondition

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
        }

        "return error when form is filled out incorrectly" when {
          "address line 1 is missing" in {
            given
              .commonPrecondition

            val result = buildRequest(route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                .post(Map(
                  "enterAddress.line1" -> Seq.empty,
                  "enterAddress.line2" -> Seq("Option lane"),
                  "enterAddress.line3" -> Seq("City name"),
                  "enterAddress.postcode" -> Seq("AB1 2XZ")
                ))

              whenReady(result) { res =>
                res.status mustBe 400
                val page = Jsoup.parse(res.body)
                page.getElementsByTag("h1").text() must include("Enter Test Unincorporated Body’s registered office address")
                page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("You must enter line 1 of the address")
              }
          }

          "address line 3 (town) is missing" in {
            given
              .commonPrecondition

            val result = buildRequest(route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                .post(Map(
                  "enterAddress.line1" -> Seq("1 street"),
                  "enterAddress.line2" -> Seq("Option lane"),
                  "enterAddress.line3" -> Seq.empty,
                  "enterAddress.postcode" -> Seq("AB1 2XZ")
                ))

              whenReady(result) { res =>
                res.status mustBe 400
                val page = Jsoup.parse(res.body)
                page.getElementsByTag("h1").text() must include("Enter Test Unincorporated Body’s registered office address")
                page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("You must enter the Town or City of the address")
              }
          }

          "Postcode is incorrectly formatted" in {
            given
              .commonPrecondition

            val result = buildRequest(route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                .post(Map(
                  "enterAddress.line1" -> Seq("1 street"),
                  "enterAddress.line2" -> Seq("Option lane"),
                  "enterAddress.line3" -> Seq("City name"),
                  "enterAddress.postcode" -> Seq("AB1 234")
                ))

              whenReady(result) { res =>
                res.status mustBe 400
                val page = Jsoup.parse(res.body)
                page.getElementsByTag("h1").text() must include("Enter Test Unincorporated Body’s registered office address")
                page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter a valid postcode")
              }
          }
        }
      }
    }
  }
}