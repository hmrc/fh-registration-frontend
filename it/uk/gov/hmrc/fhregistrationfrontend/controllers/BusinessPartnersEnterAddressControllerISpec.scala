package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnersEnterAddress
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.EnterAddressPage
import org.scalatest.TryValues.convertTryToSuccessOrFailure

class BusinessPartnersEnterAddressControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersEnterAddressController.load(1, mode).url.drop(6)

  List(NormalMode, CheckMode).foreach { mode =>

    s"GET ${route(mode)}" when {

      "the new business partners flow is enabled" should {

        "render the business partner enter address page" when {
          "there are no userAnswers" in {
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
                val line1Field = page.getElementById("enterAddress.line1")
                val line2Field = page.getElementById("enterAddress.line2")
                val line3Field = page.getElementById("enterAddress.line3")
                val postcodeField = page.getElementById("enterAddress.postcode")
                line1Field.hasAttr("value") mustBe false
                line2Field.hasAttr("value") mustBe false
                line3Field.hasAttr("value") mustBe false
                postcodeField.hasAttr("value") mustBe false
              }
            }
          }
        }

        "render the business partner enter address page" when {
          "there are userAnswers but no form data" in {
            given
              .commonPrecondition

            addUserAnswersToSession(emptyUserAnswers)

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
                val line1Field = page.getElementById("enterAddress.line1")
                val line2Field = page.getElementById("enterAddress.line2")
                val line3Field = page.getElementById("enterAddress.line3")
                val postcodeField = page.getElementById("enterAddress.postcode")
                line1Field.hasAttr("value") mustBe false
                line2Field.hasAttr("value") mustBe false
                line3Field.hasAttr("value") mustBe false
                postcodeField.hasAttr("value") mustBe false
              }
            }
          }
        }

        "render the business partner enter address page with answers" when {
          "there are userAnswers with page data" in {
            given
              .commonPrecondition

            val address = BusinessPartnersEnterAddress(
              addressLine1 = "23 High Street",
              addressLine2 = Some("Park View"),
              addressLine3 = ("Gloucester"),
              postcode = Some("NE98 1ZZ"))

            val userAnswers = emptyUserAnswers
              .set[BusinessPartnersEnterAddress](EnterAddressPage(1), address)
              .success
              .value

            addUserAnswersToSession(userAnswers)

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
                val line1Field = page.getElementById("enterAddress.line1")
                val line2Field = page.getElementById("enterAddress.line2")
                val line3Field = page.getElementById("enterAddress.line3")
                val postcodeField = page.getElementById("enterAddress.postcode")
                line1Field.attr("value") must include("23 High Street")
                line2Field.attr("value") must include("Park View")
                line3Field.attr("value") must include("Gloucester")
                postcodeField.attr("value") must include("NE98 1ZZ")
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
