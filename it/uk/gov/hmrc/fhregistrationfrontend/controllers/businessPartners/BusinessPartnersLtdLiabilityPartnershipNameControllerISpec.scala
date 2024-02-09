package uk.gov.hmrc.fhregistrationfrontend.controllers.businessPartners

import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.LtdLiabilityPartnershipName
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.LimitedLiabilityPartnershipNamePage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersLtdLiabilityPartnershipNameControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersLtdLiabilityPartnershipNameController.load(1, mode).url.drop(6)

  val pageTitle: String = "What is the name of the limited liability partnership?"
  val pageHeading: String = "What is the name of the limited liability partnership?"
  def tradingNamePageUrl(mode: Mode): String = routes.BusinessPartnersPartnershipTradingNameController.load(1, mode).url
  val characterLimitError: String = "There is a problem Limited liability partnership name must be 120 characters or less"
  val emptyTextError: String = "There is a problem Enter the name of the limited liability partnership"
  val userAnswersWithPageData = emptyUserAnswers
    .set[LtdLiabilityPartnershipName](
      LimitedLiabilityPartnershipNamePage(1),
      LtdLiabilityPartnershipName("Test Name Ltd"))
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" should {
      "render the Limited Liability Partnership Name page with answers not prepopulated" when {
        "there are userAnswers but no form data" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include(pageTitle)
            page.getElementsByTag("h1").text() must include(pageHeading)
            page.getElementById("ltdLiabilityPartnershipName").hasAttr("value") mustBe false
          }
        }
      }

      "render the Limited Liability Partnership Name page with answers prepopulated" when {
        "there are userAnswers with page data" in {
          given.commonPrecondition
          addUserAnswersToSession(userAnswersWithPageData)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include(pageTitle)
            page.getElementsByTag("h1").text() must include(pageHeading)
            page.getElementById("ltdLiabilityPartnershipName").attr("value") mustBe "Test Name Ltd"
          }
        }
      }

      "redirect to the start of the FHDDS journey" when {
        "there are no userAnswers in the database" in {
          given.commonPrecondition
          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(startCall.url)
          }
        }
      }

      s"POST ${route(mode)}" when {
        "the Limited Liability Partnership Name is entered" should {
          Map("override" -> userAnswersWithPageData, "add" -> emptyUserAnswers).foreach { case (userAnswersAction, userAnswers) =>
            s"redirect to the Trading Name page and $userAnswersAction userAnswers" in {
              given.commonPrecondition
              addUserAnswersToSession(userAnswers)

              val result = buildRequest(route(mode))
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .withHttpHeaders(xSessionId,
                  "Csrf-Token" -> "nocheck")
                .post(Map("ltdLiabilityPartnershipName" -> Seq("Partnership Name")))

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(tradingNamePageUrl(mode))
                val userAnswers = getUserAnswersFromSession.get
                val pageData = userAnswers.get(LimitedLiabilityPartnershipNamePage(1))
                pageData mustBe Some(LtdLiabilityPartnershipName("Partnership Name"))
              }
            }
          }
        }

        "Return a 400 error" when {
          "a Limited Liability Partnership Name is not entered" in {
            given.commonPrecondition
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId,
                "Csrf-Token" -> "nocheck")
              .post(Map("ltdLiabilityPartnershipName" -> Seq("")))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include(pageTitle)
              page.getElementsByTag("h1").text() must include(pageHeading)
              page.getElementsByClass("govuk-error-summary").text() must include(emptyTextError)
            }
          }

          "Limited liability partnership name is not valid (exceeds 120 chars)" in {
            given.commonPrecondition
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId,
                "Csrf-Token" -> "nocheck")
              .post(Map("ltdLiabilityPartnershipName" -> Seq("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include(pageTitle)
              page.getElementsByTag("h1").text() must include(pageHeading)
              page.getElementsByClass("govuk-error-summary").text() must include(characterLimitError)
            }
          }
        }
      }
    }
  }
}