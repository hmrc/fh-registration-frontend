package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.mvc.Http.HeaderNames

class BusinessPartnerUnincorporatedUtrControllerISpec
  extends Specifications with TestConfiguration {

  "GET /form/business-partners/partnership-self-assessment-unique-taxpayer-reference" should {

    "render the partnership-self-assessment-unique-taxpayer-reference page" in {
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/form/business-partners/partnership-self-assessment-unique-taxpayer-reference")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("Does the partner have a Self Assessment Unique Taxpayer Reference (UTR)?")
          page.getElementsByTag("h1").text() must include("Does test partner have a Self Assessment Unique Taxpayer Reference (UTR)?")
        }
      }
    }
  }

  "POST /form/business-partners/partnership-self-assessment-unique-taxpayer-reference" when {
    "yes is selected and the UTR is entered" should {
      "redirect to the Partnership Registered Office Address page" in {
        given
          .commonPrecondition

        val result = buildRequest("/form/business-partners/partnership-self-assessment-unique-taxpayer-reference")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map(
            "uniqueTaxpayerReference_yesNo" -> Seq("true"),
            "uniqueTaxpayerReference_value" -> Seq("1234567890")
          ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/partnership-registered-office-address")
        }
      }
    }

    "no is selected" should {
      "redirect to the Partnership Registered Office Address page" in {
        given
          .commonPrecondition

        val result = buildRequest("/form/business-partners/partnership-self-assessment-unique-taxpayer-reference")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("uniqueTaxpayerReference_yesNo" -> Seq("false")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/partnership-registered-office-address")
        }
      }
    }

    "no option selected" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partnership-self-assessment-unique-taxpayer-reference")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("uniqueTaxpayerReference_yesNo" -> Seq.empty))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select whether they have a Self Assessment Unique Taxpayer Reference")
          }
        }
      }
    }

    "option selected but UTR not supplied" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partnership-self-assessment-unique-taxpayer-reference")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("uniqueTaxpayerReference_yesNo" -> Seq("true")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter a Self Assessment Unique Taxpayer Reference (UTR)")
          }
        }
      }
    }

    "the form hasUtr field is invalid format" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partnership-self-assessment-unique-taxpayer-reference")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("uniqueTaxpayerReference_yesNo" -> Seq("error")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select whether they have a Self Assessment Unique Taxpayer Reference (UTR)")
          }
        }
      }
    }

    "the UTR field is invalid format" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partnership-self-assessment-unique-taxpayer-reference")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("uniqueTaxpayerReference_yesNo" -> Seq("true"),
              "uniqueTaxpayerReference_value" -> Seq("1234")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter a valid Self Assessment Unique Taxpayer Reference (UTR)")
          }
        }
      }
    }
  }
}