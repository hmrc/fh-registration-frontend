package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersCannotFindAddressControllerISpec
  extends Specifications with TestConfiguration {

  "GET /business-partners/cannot-find-address" when {

    "the new business partners flow is enabled" should {

      "render the Business Partners Cannot Find Address page" when {
        "the user is authenticated" in {
          given
            .commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/business-partners/cannot-find-address")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("We cannot find any addresses for HR33 7GP")
            }
          }
        }
      }
    }
  }
}