package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

import scala.collection.immutable.Seq

class BusinessPartnersAddressControllerISpec
  extends Specifications with TestConfiguration {

  val route = "/business-partners/partner-address"

  s"GET $route" when {

    "the new business partners flow is enabled" should {

      "render the business partner address page" when {
        "the user is authenticated" in {
          given
            .commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(baseUrl + route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("What is the partner’s address?")
              page.getElementsByTag("h1").text() must include("What is Test User’s address?")
            }
          }
        }
      }
    }


  }

  s"POST $route" when {
    "the postcode is valid, address line provided and addresses are returned from address lookup" should {
      "redirect to choose address" in {
        given
          .commonPreconditionWithMultipleAddressLookup(true)

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
          "Csrf-Token" -> "nocheck")
          .post(Map("partnerAddressLine" -> Seq("1"),
            "partnerPostcode" -> Seq("AB1 2YZ")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/business-partners/choose-address")
        }
      }
    }

    "the postcode is valid, address line not provided and addresses are returned from address lookup" should {
      "redirect to choose address" in {
        given
          .commonPreconditionWithMultipleAddressLookup(true)


        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
          "Csrf-Token" -> "nocheck")
          .post(Map("partnerAddressLine" -> Seq.empty,
            "partnerPostcode" -> Seq("AB1 2YZ")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/business-partners/choose-address")
        }
      }
    }

    "the postcode is valid, address line not provided and address lookup returns an error" should {
      "return 400" in {
        given
          .commonPreconditionWithMultipleAddressLookup(false)

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
          "Csrf-Token" -> "nocheck")
          .post(Map("partnerAddressLine" -> Seq.empty,
            "partnerPostcode" -> Seq("AB1 2YZ")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("Sorry, there was problem performing this search, please try again and if the problem persists then enter the address manually")
          }
        }
      }

    "postcode not populated" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq("1"),
                      "partnerPostcode"    -> Seq.empty))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("Enter the postcode of the address")
          }
        }
      }
    }

    "postcode invalid format" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq("1"),
                      "partnerPostcode"    -> Seq("A")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter a valid postcode")
          }
        }
      }
    }

    "address line contains invalid characters" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)
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
    }

    "address line contains more than 35 characters" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)
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
}