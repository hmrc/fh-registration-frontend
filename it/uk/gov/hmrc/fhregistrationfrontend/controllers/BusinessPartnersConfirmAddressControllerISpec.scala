package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{Mode, NormalMode, CheckMode}
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersConfirmAddressControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersConfirmAddressController.load(1, mode).url.drop(6)

  List(NormalMode, CheckMode).foreach{ mode =>
    s"GET $baseUrl${route(mode)}" when {

      "the new business partners flow is enabled" should {

        "render the business partner confirm address page" when {
          "the user is authenticated" in {
            addUserAnswersToSession(emptyUserAnswers)
            given.commonPrecondition

            WsTestClient.withClient { client =>
              val result = client.url(baseUrl + route(mode))
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

      "there are no answers in the database" should {
        "redirect to the start of BusinessPartners journey" in {
          given.commonPrecondition

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersController.load(1, mode).url)
          }
        }
      }
    }

    s"POST ${route(mode)}" when {
      "navigate to the check your answers page" when {
        "the user is authenticated and the user answers are present" in {
          addUserAnswersToSession(emptyUserAnswers)
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(baseUrl + route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck").withFollowRedirects(false)
              .post(Map("" -> Seq.empty))

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersCheckYourAnswersController.load().url)
            }
          }
        }

      }

      "there are no answers in the database" should {
        "redirect to the start of BusinessPartners journey" in {
          given.commonPrecondition


          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersController.load(1, mode).url)
          }
        }
      }
    }
  }
}
