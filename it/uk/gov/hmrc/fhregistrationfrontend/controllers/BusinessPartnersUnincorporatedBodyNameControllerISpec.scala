package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.models.UnincorporatedBodyName
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.UnincorporatedBodyNamePage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import org.scalatest.TryValues.convertTryToSuccessOrFailure

class BusinessPartnersUnincorporatedBodyNameControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersUnincorporatedBodyNameController.load(index = 1, mode).url.drop(6)

  val userAnswersWithPageData = emptyUserAnswers.set[UnincorporatedBodyName](UnincorporatedBodyNamePage(1), UnincorporatedBodyName("company name"))
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" should {
      "render the Unincorporated Body Name page" when {
        "there is no form data" in {
          given
            .commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include("What is the unincorporated body name?")
            page.getElementsByTag("h1").text() must include("What is the unincorporated body name?")
          }
        }
      }

      "there is form data" in {
        given
          .commonPrecondition
        addUserAnswersToSession(userAnswersWithPageData)

        val result = buildRequest(route(mode))
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("What is the unincorporated body name?")
          page.getElementsByTag("h1").text() must include("What is the unincorporated body name?")
          val companyName = page.getElementById("unincorporatedBodyName_value")
          companyName.attr("value") mustBe "company name"
        }
      }

      "redirect the user to the start of the BusinessPartners journey" when {
        "there is no user answers in the database" in {
          given.commonPrecondition
          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.Application.main().url)
          }
        }
      }
    }

    s"POST ${route(mode)}" when {
      val unincorpBodyTradingNameUrl: String = routes.BusinessPartnersUnincorporatedBodyTradingNameController.load().url
      "the unincorporated body name is entered" should {
        "redirect to the Unincorporated Body Trading Name page" in {
          given
            .commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map(
              "unincorporatedBodyName_value" -> Seq("Test Body")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(unincorpBodyTradingNameUrl)
          }
        }
      }

      "Unincorporated body name is not supplied" should {
        "return 400" in {
          given
            .commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("unincorporatedBodyName_value" -> Seq("")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter an unincorporated body name")
          }
        }
      }

      "Unincorporated body name is not valid (exceeds 120 chars)" should {
        "return 400" in {
          given
            .commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("unincorporatedBodyName_value" -> Seq("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Unincorporated body name must be 120 characters or less")
          }
        }
      }
    }
  }
}
