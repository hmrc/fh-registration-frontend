package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerType
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersCannotFindAddressControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersCannotFindAddressController.load(1, mode).url.drop(6)

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET $baseUrl${route(mode)}" when {

      "the new business partners flow is enabled" should {
        "render the Business Partners Cannot Find Address page" when {
          BusinessPartnerType.partnerTypes.foreach { partnerType =>
            s"the user has a partnerType of ${partnerType.toString}" in {
              given
                .commonPrecondition
              addUserAnswersToSession(userAnswersWithBusinessPartnerType(partnerType))

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

        "redirect to business partners page" when {
          "the user answers does not contain the business type" in {
            given
              .commonPrecondition
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersController.load(1, mode).url)
            }
          }

          "the user answers does not exist" in {
            given
              .commonPrecondition

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
}