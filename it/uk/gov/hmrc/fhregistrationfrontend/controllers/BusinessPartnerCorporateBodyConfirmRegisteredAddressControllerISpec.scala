package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerCorporateBodyConfirmRegisteredAddressControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersCorporateBodyConfirmRegisteredAddressController.load().url.drop(6)
  val checkYourAnswersUrl: String = routes.BusinessPartnersCheckYourAnswersController.load("corporate-body").url

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
            page.title() must include("Confirm the company’s registered office address?")
            page.getElementsByTag("h1").text() must include("Confirm the Test Corp’s registered office address")
          }
        }
      }
    }
  }

  s"POST $route" when {

    "the user clicks save and continue" should {
      "redirect to the Check Your Answers page" when {
        "the user is authenticated" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "mock" -> Seq("true"),
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(checkYourAnswersUrl)
          }
        }
      }
    }
  }
}