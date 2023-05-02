package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

import scala.collection.immutable.Seq

class BusinessPartnersChooseAddressControllerISpec
  extends Specifications with TestConfiguration {

  "GET /form/business-partners/choose-address" should {

      "render the choose address page" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/choose-address")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()


          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include("Choose address")
          }
        }
      }
    }

  "POST /form/business-partners/choose-address" when {
    "the form has no errors" should {
      //Todo this will change when navigation is implemented
      "redirect the user to the 'confirm the partner's address" in {
        given.commonPrecondition

          val result = buildRequest("/form/business-partners/choose-address")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "chosenAddress" -> Seq("1")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/form/business-partners/confirm-partner-address")
          }
      }
    }

    "no address in selected" should {
      //Todo this will change when navigation is implemented
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/choose-address")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("chosenAddress" -> Seq.empty))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select an address")
          }
        }
      }
    }

    "the form is invalid format" should {
      //Todo this will change when navigation is implemented
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/choose-address")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("chosenAddress" -> Seq("xyz")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select an address")
          }
        }
      }
    }

    "the address key is out of range" should {
      //Todo this will change when navigation is implemented
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/choose-address")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("chosenAddress" -> Seq("100")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select an address")
          }
        }
      }
    }
  }
}
