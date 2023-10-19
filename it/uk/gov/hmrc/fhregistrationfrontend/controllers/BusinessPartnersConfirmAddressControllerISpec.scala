package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersConfirmAddressControllerISpec
  extends Specifications with TestConfiguration {

  val route = "/business-partners/confirm-partner-address"

  s"GET $route" when {

    "the new business partners flow is enabled" should {

      "render the business partner confirm address page" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(baseUrl + route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("Confirm the partner’s address?")
              page.getElementsByTag("h1").text() must include("Confirm test business partner’s address")
            }
          }
        }
      }
    }
  }

  s"POST $route" when {
    "navigate to the check your answers page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(baseUrl + route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck").withFollowRedirects(false)
            .post(Map("" -> Seq.empty))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/business-partners/check-your-answers?partnerType=individual")
          }
        }
      }
    }
  }
}