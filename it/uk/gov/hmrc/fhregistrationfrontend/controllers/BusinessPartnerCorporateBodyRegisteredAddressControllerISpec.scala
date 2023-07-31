package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerCorporateBodyRegisteredAddressControllerISpec
  extends Specifications with TestConfiguration {

  val requestURL = "/form/business-partners/corporate-body-registered-office-address"

  "GET /form/business-partners/corporate-body-registered-office-address" when {

    "the new business partners flow is enabled" should {

      "render the business partner corporate body registered address page" when {
        "the user is authenticated" in {
          given.commonPrecondition

          val result = buildRequest(requestURL)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("What is the company’s registered office address?")
              page.getElementsByTag("h1").text() must include("What is Test Corporate Body’s registered office address?")
            }
          }
        }
      }
    }

  "POST /form/business-partners/corporate-body-registered-office-address" when {
    "the form has no errors and multiple addresses are found" should {
      "return 303" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl$requestURL")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck").withFollowRedirects(false)
            .post(Map("partnerAddressLine" -> Seq("Drury Lane"),
              "partnerPostcode" -> Seq("AB1 2YZ")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/form/business-partners/choose-address")
          }
        }
      }
    }

    "the form has no errors and a matching address is found" should {
      "return 303" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl$requestURL")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck").withFollowRedirects(false)
            .post(Map("partnerAddressLine" -> Seq("1 Romford Road"),
              "partnerPostcode" -> Seq("TF1 4ER")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/form/business-partners/confirm-corporate-body-registered-office-address")
          }
        }
      }
    }

    "postcode supplied but address line not populated" should {
      "return 303" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl$requestURL")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck").withFollowRedirects(false)
            .post(Map("partnerAddressLine" -> Seq.empty,
              "partnerPostcode" -> Seq("AB1 2YZ")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/form/business-partners/choose-address")
          }
        }
      }
    }

    "address cannot be found for postcode" should {
      "return 303" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl$requestURL")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck").withFollowRedirects(false)
            .post(Map("partnerAddressLine" -> Seq.empty,
              "partnerPostcode" -> Seq("AB1 2YX")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/business-partners/cannot-find-address")
          }
        }
      }
    }

    "postcode not populated" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl$requestURL")
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
    }

    "postcode invalid format" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl$requestURL")
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
    }

    "address line contains invalid characters" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl$requestURL")
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
          val result = client.url(s"$baseUrl$requestURL")
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