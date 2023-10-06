package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerCorporateBodyConfirmRegisteredAddressControllerISpec
  extends Specifications with TestConfiguration {

  "GET /form/business-partners/confirm-corporate-body-registered-office-address" when {

    "the new business partners flow is enabled" should {

      "render the business partner confirm address page" when {
        "the user is authenticated" in {
          given
            .commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/form/business-partners/confirm-corporate-body-registered-office-address")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("Confirm the company’s registered office address?")
              page.getElementsByTag("h1").text() must include("Confirm the Test Corp’s registered office address")
            }
          }
        }
      }
    }
  }

  "POST /form/business-partners/confirm-corporate-body-registered-office-address" when {

    "the user clicks save and continue" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/form/business-partners/confirm-corporate-body-registered-office-address")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "mock" -> Seq("true"),
              ))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Form submitted, with result:")
            }
          }
        }
      }
    }
  }
}