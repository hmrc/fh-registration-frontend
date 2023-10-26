package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.mvc.Http.HeaderNames

class BusinessPartnerPartnershipRegisteredAddressControllerISpec
  extends Specifications with TestConfiguration {

  val requestURL = "/form/business-partners/partnership-registered-office-address"
  val route = routes.BusinessPartnerPartnershipRegisteredAddressController.load().url.drop(6)

  s"GET $route" when {

    "the new business partners flow is enabled" should {

      "render the business partner address page" when {
        "the user is authenticated" in {
          given
            .commonPrecondition

          val result = buildRequest(route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("What is the partnership’s registered office address?")
              page.getElementsByTag("h1").text() must include("What is Test User’s registered office address?")
            }
        }
      }
    }
  }

  s"POST $route" when {
    "address entered where single address found" should {
      "redirect to the Confirm Address page" in {
        given
          .commonPreconditionWithSingleAddressLookup(true)

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map(
            "partnerAddressLine" -> Seq("1 test drive"),
            "partnerPostcode" -> Seq("AA1 2BB")
          ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersConfirmPartnershipRegisteredAddressController.load().url)
          }
      }
    }

    "address entered where multiple found" should {
      "redirect to the Choose Address page" in {
        given
          .commonPreconditionWithMultipleAddressLookup(true)

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map(
            "partnerAddressLine" -> Seq.empty,
            "partnerPostcode" -> Seq("TF1 4ER")
          ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersChooseAddressController.load().url)
          }
        }
      }

    /*"address entered where none found" should {
      "redirect to the Cannot Find Address page" in {
        given
          .commonPrecondition

        val result = buildRequest("/form/business-partners/partnership-registered-office-address")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map(
            "partnerAddressLine" -> Seq.empty,
            "partnerPostcode" -> Seq("HR33 7GP")
          ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/business-partners/cannot-find-address")
        }
      }
    }*/

    "postcode not populated" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq("1"),
              "partnerPostcode" -> Seq.empty))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("Enter the postcode of the address")
          }
      }
    }

    "postcode invalid format" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq("1"),
              "partnerPostcode" -> Seq("A")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter a valid postcode")
          }
      }
    }

    "address line contains invalid characters" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq("The lane;"),
              "partnerPostcode" -> Seq("AB1 2YZ")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("only include letters a to z, numbers, apostrophes, commas, dashes, exclamation marks, forward slashes, full stops, hyphens, quotation marks, round brackets and spaces")
          }
      }
    }

    "address line contains more than 35 characters" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq("qwertyuiopasdfghjklzxcvbnmqwkydvkdsgvisudgfkjsdvkjsdcjkdh"),
              "partnerPostcode" -> Seq("AB1 2YZ")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("Address lines must not be longer than 35 characters")
          }
      }
    }
  }
}