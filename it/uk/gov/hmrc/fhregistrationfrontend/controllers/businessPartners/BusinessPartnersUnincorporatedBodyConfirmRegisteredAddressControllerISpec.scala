package uk.gov.hmrc.fhregistrationfrontend.controllers.businessPartners

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersUnincorporatedBodyConfirmRegisteredAddressControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersUnincorporatedBodyConfirmRegisteredAddressController.load().url.drop(6)

  s"GET $route" when {

    "the new business partners flow is enabled" should {

      "render the business partner confirm address page" when {
        "the user is authenticated" in {
          given
            .commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include("Confirm the unincorporated body’s registered office address?")
            page.getElementsByTag("h1").text() must include("Confirm the Test Corp’s registered office address")
          }
        }
      }
    }
  }

  s"POST $route" when {

    "the user clicks save and continue" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          val result = buildRequest(route)
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
