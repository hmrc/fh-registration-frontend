package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{NormalMode, CheckMode, Mode}
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersCannotFindAddressControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersCannotFindAddressController.load(1, mode).url.drop(6)

  List(NormalMode, CheckMode).foreach { mode =>

    s"GET $baseUrl${route(mode)}" when {

      "the new business partners flow is enabled" should {

        "render the Business Partners Cannot Find Address page" when {
          "the user is authenticated" in {
            given
              .commonPrecondition

            val result = buildRequest(route(mode))
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