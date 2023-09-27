package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.mvc.Http.HeaderNames

class BusinessPartnersCorporateBodyVatNumberControllerISpec
  extends Specifications with TestConfiguration {

  "GET /form/business-partners/corporate-body-vat-registration-number" should {

    "render the corporate-body-vat-registration-number page" in {
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/form/business-partners/corporate-body-vat-registration-number")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("Does the corporate body have a UK VAT registration number?")
          page.getElementsByTag("h1").text() must include("Does test corporateBody have a UK VAT registration number?")
        }
      }
    }
  }

  "POST /form/business-partners/corporate-body-vat-registration-number" when {
    "yes is selected and the vatnumber entered" should {
      "redirect to the Corporate Body Registered Office Address page" in {
        given
          .commonPrecondition

        val result = buildRequest("/form/business-partners/corporate-body-vat-registration-number")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map(
            "vatNumber_yesNo" -> Seq("true"),
            "vatNumber_value" -> Seq("123456789")
          ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/corporate-body-registered-office-address")
        }
      }
    }

    "no is selected" should {
      "redirect to the Corporate Body UTR page" in {
        given
          .commonPrecondition

        val result = buildRequest("/form/business-partners/corporate-body-vat-registration-number")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("vatNumber_yesNo" -> Seq("false")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/corporate-body-corporation-tax-unique-taxpayer-reference")
        }
      }
    }

    "no hasVat number selected" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/corporate-body-vat-registration-number")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
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
          val result = client.url(s"$baseUrl/form/business-partners/corporate-body-vat-registration-number")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
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
          val result = client.url(s"$baseUrl/form/business-partners/corporate-body-vat-registration-number")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
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
          val result = client.url(s"$baseUrl/form/business-partners/corporate-body-vat-registration-number")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
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