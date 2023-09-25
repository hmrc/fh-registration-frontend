package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.mvc.Http.HeaderNames

class BusinessPartnersVatRegistrationNumberControllerISpec
  extends Specifications with TestConfiguration {

  val route = "/form/business-partners/partner-vat-registration-number"

  s"GET $route" should {

    "render the partner-vat-registration-number page" in {
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(baseUrl + route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("Does the partner have a UK VAT registration number?")
          page.getElementsByTag("h1").text() must include("Does test partner have a UK VAT registration number?")
        }
      }
    }
  }

  s"POST $route" when {
    "yes is selected and the vatnumber entered" should {
      "return 303 and redirect to What is the test partner’s address? page" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck").withFollowRedirects(false)
            .post(Map("vatNumber_yesNo" -> Seq("true"),
              "vatNumber_value" -> Seq("123456789")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"${routes.BusinessPartnerAddressController.load().url}")
          }
        }
      }
    }

    "no is selected" should {
      "return 303 and redirect to Does the partner have a Self Assessment Unique Taxpayer Reference (UTR)? page" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck").withFollowRedirects(false)
            .post(Map("vatNumber_yesNo" -> Seq("false")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"${routes.BusinessPartnerSoleProprietorUtrController.load().url}")
          }
        }
      }
    }

    "no hasVat number selected" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("vatNumber_yesNo" -> Seq.empty))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select whether the business has a VAT registration number")
          }
        }
      }
    }

    "hasVat number selected but vatNumber not supplied" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("vatNumber_yesNo" -> Seq("true")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter the VAT registration number")
          }
        }
      }
    }

    "the form hasVat field is invalid format" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("vatNumber_yesNo" -> Seq("error")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select whether the business has a VAT registration number")
          }
        }
      }
    }

    "the vatnumber field is invalid format" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("vatNumber_yesNo" -> Seq("true"),
              "vatNumber_value" -> Seq("1234")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter a valid UK VAT registration number")
          }
        }
      }
    }
  }
}
