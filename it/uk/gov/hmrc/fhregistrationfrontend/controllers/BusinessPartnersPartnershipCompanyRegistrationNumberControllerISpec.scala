package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersPartnershipCompanyRegistrationNumberControllerISpec
  extends Specifications with TestConfiguration {

  "GET /business-partners/partnership-company-registration-number" should {
    "render the partnership-company-registration-number page" in {
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/business-partners/partnership-company-registration-number")
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "limited-liability-partnership")
          )
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("What is the partnership’s company registration number?")
          page.getElementsByTag("h1").text() must include("What is Test Partnership’s company registration number?")
          page.getElementsByTag("p").text() must include("You can search Companies House for a company registration number (opens in new tab)")
        }
      }
    }
  }

  "POST /business-partners/partnership-company-registration-number" when {
    "the companyRegistrationNumber is entered" should {
      "return 200 with companyRegistrationNumber" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/business-partners/partnership-company-registration-number")
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "limited-liability-partnership")
            )
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("companyRegistrationNumber" -> Seq("01234567")))

          whenReady(result) { res =>
            res.status mustBe 200
            res.body mustBe "Next page! with companyRegistrationNumber: 01234567"
          }
        }
      }

      "return 200 with letter formatted companyRegistrationNumber" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/business-partners/partnership-company-registration-number")
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "limited-liability-partnership")
            )
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("companyRegistrationNumber" -> Seq("AB123456")))

          whenReady(result) { res =>
            res.status mustBe 200
            res.body mustBe "Next page! with companyRegistrationNumber: AB123456"
          }
        }
      }
    }

    "companyRegistrationNumber is not supplied" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/business-partners/partnership-company-registration-number")
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "limited-liability-partnership")
            )
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("companyRegistrationNumber" -> Seq("")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter the company registration number")
          }
        }
      }
    }

    "the companyRegistrationNumber field is invalid format" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/business-partners/partnership-company-registration-number")
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "limited-liability-partnership")
            )
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("companyRegistrationNumber" -> Seq("aaa")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Company registration number must be 8 numbers, or 2 letters followed by 6 numbers")
          }
        }
      }
    }
  }
}
