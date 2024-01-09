package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.http.HeaderNames
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerType
import uk.gov.hmrc.fhregistrationfrontend.models.des.PartnerType
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnerTypePage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerControllerISpec
  extends Specifications with TestConfiguration {

  val index = 1
  val modes = List(NormalMode, CheckMode)


  modes.foreach { mode =>
    val route = routes.BusinessPartnersController.load(1, mode)
    s"GET ${route.url}" should {
      "render the business partners page" that {
        "has the no items selected" when {
          "there is no useranswers" in {
            given.commonPrecondition
            val result = buildRequestFromRoute(route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include("Business partners - Apply for the Fulfilment House Due Diligence Scheme")
              println("&&&&&&&&&&&&&&&")
              println(page)
            }
          }
        }

        BusinessPartnerType.values.foreach { partnerType =>

          s"has $partnerType selected" when {
            s"the user has previously selected $partnerType" in {
              given.commonPrecondition
              val userAnswers = emptyUserAnswers.set(PartnerTypePage(1), partnerType).success.value
              addUserAnswersToSession(userAnswers)

              val result = buildRequestFromRoute(route)
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .get()

              whenReady(result) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title must include("Business partners - Apply for the Fulfilment House Due Diligence Scheme")
                println("&&&&&&&&&&&&&&&")
                println(page)
              }
            }
          }
        }
      }
    }

//    s"POST ${route.url}" should {
//      "the user clicks save and continue" should {
//        "redirect to the correct page" when {
//          "the user is authenticated and select Individual" in {
//            given.commonPrecondition
//
//            val result = buildRequest("/business-partners")
//              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
//              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
//              .post(Map(
//                "businessType" -> Seq("Individual"),
//              ))
//
//            whenReady(result) { res =>
//              res.status mustBe 303
//              res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/business-partners/partner-name/1")
//            }
//          }
//        }
//
//        "return 400" when {
//          "the user is authenticated and Nothing is selected" in {
//            given.commonPrecondition
//
//            val result = buildRequest("/business-partners")
//              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
//              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
//              .post(Map("businessType" -> Seq.empty))
//
//            whenReady(result) { res =>
//              res.status mustBe 400
//              val page = Jsoup.parse(res.body)
//              page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select the type of legal entity for the partner")
//            }
//          }
//        }
//      }
//    }
  }
}
