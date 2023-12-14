package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.mvc.Http.HeaderNames

class BusinessPartnersEnterAddressControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersEnterAddressController.load(1, mode).url.drop(6)

  List(NormalMode, CheckMode).foreach { mode =>

    s"GET ${route(mode)}" when {

      "the new business partners flow is enabled" should {

        "render the business partner enter address page" when {
          "the user is authenticated" in {
            given
              .commonPrecondition

            WsTestClient.withClient { client =>
              val result = client.url(baseUrl + route(mode))
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .get()

              whenReady(result) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title() must include("Enter the partner’s address?")
                page.getElementById("page-heading").text() must include("Enter")
                page.getElementById("page-heading").text() must include("address")
              }
            }
          }
        }
      }
    }

    s"POST ${route(mode)}" should {
      "redirect to the Check Your Answers page" when {
        "all address fields are populated" in {
          given.commonPrecondition

          val result = buildRequest(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie)
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "enterAddress.line1" -> Seq("1"),
              "enterAddress.line2" -> Seq("Old Town"),
              "enterAddress.line3" -> Seq("Cityville"),
              "enterAddress.postcode" -> Seq("AA1 2YZ")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/business-partners/check-your-answers?partnerType=individual")
          }
        }

        "only mandatory fields are populated" in {
          given.commonPrecondition

          val result = buildRequest(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie)
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "enterAddress.line1" -> Seq("1"),
              "enterAddress.line3" -> Seq("Cityville")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/business-partners/check-your-answers?partnerType=individual")
          }
        }
      }

      "return an error" when {
        "mandatory fields are not populated" in {
          given.commonPrecondition

          val result = buildRequest(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie)
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "enterAddress.line1" -> Seq(""),
              "enterAddress.line3" -> Seq("")
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include(
              "There is a problem You must enter line 1 of the address You must enter the Town or City of the address"
            )
          }
        }
      }
    }
  }
}
