package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerControllerISpec
  extends Specifications with TestConfiguration {


  "GET /form/business-partner" when {

    "render the business partners page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/business-partners")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("Business partners - Apply for the Fulfilment House Due Diligence Scheme")
          }
        }
      }
    }

  }

  "POST /business-partners" when {
    "the user clicks save and continue" should {
      "redirect to the correct page" when {
        "the user is authenticated and select Individual" in {
          given.commonPrecondition

          val result = buildRequest("/business-partners")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "businessType" -> Seq("Individual"),
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/business-partners/partner-name")
          }
        }
      }

      "return 400" when {
        "the user is authenticated and Nothing is selected" in {
          given.commonPrecondition

          val result = buildRequest("/business-partners")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("businessType" -> Seq.empty))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select the type of legal entity for the partner")
          }
        }
      }
    }
  }
}
