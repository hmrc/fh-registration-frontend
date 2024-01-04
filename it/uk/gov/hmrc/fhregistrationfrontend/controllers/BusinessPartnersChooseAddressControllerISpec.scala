package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.ChooseAddressPage
import models.{CheckMode, Mode, NormalMode}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.ChooseAddress
import org.scalatest.TryValues.convertTryToSuccessOrFailure

import scala.collection.immutable.Seq

class BusinessPartnersChooseAddressControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersChooseAddressController.load(1, mode).url.drop(6)

  val chosenAddress = ChooseAddress("1 Romford Road, Wellington, Telford, TF1 4ER")

  val userAnswersWithPageData = emptyUserAnswers
    .set[ChooseAddress](ChooseAddressPage(1), chosenAddress)
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>

    s"GET ${route(mode)}" should {

      "render the choose address page" in {
        given
          .commonPrecondition

        addUserAnswersToSession(emptyUserAnswers)

        val result = buildRequest(route(mode))
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("Choose address")
        }
      }

      "there are no user answers in the database" should {
        "redirect to the start of BusinessPartners" in {
          given.commonPrecondition
          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersController.load().url)
          }
        }
      }
    }

    s"POST ${route(mode)}" when {
      "redirect the user to the Check Your Answers page" when {
        "the form has no errors" in {
          given.commonPrecondition

          addUserAnswersToSession(userAnswersWithPageData)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "chosenAddress" -> Seq("1")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersCheckYourAnswersController.load().url)
          }
        }
      }
    }
  }
}
