package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersEnterRegistrationOfficeAddressControllerSpec
  extends Specifications with TestConfiguration {

  "GET /form/business-partners/enter-partnership-registered-office-address" when {

    "the new business partners flow is enabled" should {

      "render the business partner enter partnership registration office address page" when {
        "the user is authenticated" in {
          given
            .commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/form/business-partners/enter-partnership-registered-office-address")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("Enter the partnership’s registered office address?")
              page.getElementById("page-heading").text() must include("Enter")
              page.getElementById("page-heading").text() must include("registered office address?")
            }
          }
        }
      }
    }
  }


}
