package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.mvc.Http.HeaderNames

class BusinessPartnersCorporateBodyCompanyRegistrationNumberControllerISpec
  extends Specifications with TestConfiguration {

  val route = "/form/business-partners/company-registration-number"

  s"GET $route" should {
    "render the corporate-body-company-registration-number page" in {
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(baseUrl + route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("What is the company’s company registration number?")
          page.getElementsByTag("h1").text() must include("What is Test CorporateBody’s company registration number?")
          page.getElementsByTag("p").text() must include("You can search Companies House for a company registration number (opens in new tab)")
        }
      }
    }
  }

  s"POST $route" when {
    "the companyRegistrationNumber is entered" should {
      "return 303 with companyRegistrationNumber" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "corporateBody")
            )
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck").withFollowRedirects(false)
            .post(Map("companyRegistrationNumber" -> Seq("01234567")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/corporate-body-vat-registration-number")
          }
        }
      }

      "return 303 with letter formatted companyRegistrationNumber" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "corporateBody")
            )
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck").withFollowRedirects(false)
            .post(Map("companyRegistrationNumber" -> Seq("AB123456")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/corporate-body-vat-registration-number")
          }
        }
      }
    }

    "companyRegistrationNumber is not supplied" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "corporateBody")
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
          val result = client.url(baseUrl + route)
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "corporateBody")
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
