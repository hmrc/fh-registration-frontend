package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.PartnerNameForm.{firstNameKey, lastNameKey}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnershipNamePage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersPartnershipNameControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersPartnershipNameController.load(1, mode).url.drop(6)
  val pageHeading: String = "What is the name of the partnership?"
  val pageTitle: String = "What is the name of the partnership? - Business partners"
  val emptyTextError: String = "Enter a partnership name"
  val characterLimitError: String = "Partnership name must be 120 characters or less"
  val tradingNamePageUrl: String = routes.BusinessPartnersPartnershipTradingNameController.load(1, NormalMode).url
  val userAnswersWithPageData = emptyUserAnswers
    .set[String](PartnershipNamePage(1), "partnershipName")
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" when {
      "when the user is authenticated" should {
        "render the business partner partnership name page with answers not prepopulated" when {
          "there are useranswers but no form data" in {
            given.commonPrecondition

            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(pageTitle)
              page.getElementsByTag("h1").text must include(pageHeading)
              val partnershipName = page.getElementById("partnershipName")
              partnershipName.hasAttr("value") mustBe false
            }
          }
        }

        "render the business partner IndividualsAndSoleProprietors partner name page with answers prepopulated" when {
          "there is are userAnswers with page data" in {
            given.commonPrecondition
            addUserAnswersToSession(userAnswersWithPageData)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(pageTitle)
              println("UUUUUUUUUUU")
              println(page)
              page.getElementsByTag("h1").text must include(pageHeading)
              val partnershipName = page.getElementById("partnershipName")
              partnershipName.attr("value") mustBe "partnershipName"
            }
          }
        }
      }

      "there is no user answers in the database" should {
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
      "the form is filled out correctly" should {
        Map("override" -> userAnswersWithPageData, "add" -> emptyUserAnswers).foreach { case (uaAction, userAnswers) =>
          s"redirect to trading name page and $uaAction userAnswers" in {
            given.commonPrecondition
            addUserAnswersToSession(userAnswers)

            val result = buildRequest(route(mode))
              .addCookies(
                DefaultWSCookie("mdtp", authAndSessionCookie),
                DefaultWSCookie("businessType", "individual")
              )
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map("partnershipName" -> Seq("test")))

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersPartnershipTradingNameController.load(1, mode).url)
              val userAnswers = getUserAnswersFromSession.get
              val pageData = userAnswers.get(PartnershipNamePage(1))
              pageData mustBe Some("test")
            }
          }
        }
      }

      "return 400" when {
        "the user doesn't enter a partnership name" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("partnershipName" -> Seq("")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include(pageTitle)
            page.getElementsByTag("h1").text() must include(pageHeading)
            page.getElementById("partnershipName-error").text() must include(emptyTextError)
          }
        }

        "user enters over 120 characters" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("partnershipName" -> Seq("hghghghghghghghhghghghghghghghhghghgh" +
              "ghghghghhghghghghghghghhghghghghghghghhghghghghghghghhghghgh" +
              "ghghghghhghghghghghghghhghghghghghghghhghghghghghghghhghghghg")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include(pageTitle)
            page.getElementsByTag("h1").text() must include(pageHeading)
            page.getElementById("partnershipName-error").text() must include(characterLimitError)
          }
        }
      }

      "there is no user answers in the database" should {
        "redirect to the start of BusinessPartners" in {
          given.commonPrecondition
          val result = buildRequest(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "partnership")
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(firstNameKey -> Seq("Coca"), lastNameKey -> Seq("Cola")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersController.load().url)
          }
        }
      }
    }
  }
}