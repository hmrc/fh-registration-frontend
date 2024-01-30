package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.http.HeaderNames
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.CompanyNamePage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersCorporateBodyCompanyNameControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersCorporateBodyCompanyNameController.load(1, mode).url.drop(6)
  val pageHeading: String = "What is the company name?"
  val pageTitle: String = "What is the company name? - Business partners"
  val emptyTextError: String = "Enter a company name"
  val characterLimitError: String = "Company name must be 140 characters or less"
  val corpBodyTradingNameUrl: String = routes.BusinessPartnersCorporateBodyTradingNameController.load().url
  val userAnswersWithPageData = emptyUserAnswers
    .set[String](CompanyNamePage(1), "companyName")
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" when {
      "when the user is authenticated" should {
        "render the business partners corporate body company name page with answers not prepopulated" when {
          "there are user answers but no form data" in {
            given.commonPrecondition

            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(pageTitle)
              page.getElementsByTag("h1").text must include(pageHeading)
              val companyName = page.getElementById("companyName")
              companyName.hasAttr("value") mustBe false
            }
          }
        }

        "render the business partner corporate body company name page with answers pre-populated" when {
          "there is userAnswers with page data" in {
            given.commonPrecondition
            addUserAnswersToSession(userAnswersWithPageData)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(pageTitle)
              page.getElementsByTag("h1").text must include(pageHeading)
              val companyName = page.getElementById("companyName")
              companyName.attr("value") mustBe "companyName"
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
                .post(Map("companyName" -> Seq("test")))

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(corpBodyTradingNameUrl)
                val userAnswers = getUserAnswersFromSession.get
                val pageData = userAnswers.get(CompanyNamePage(1))
                pageData mustBe Some("test")
              }
            }
          }
        }

        "User does not enter a company name" should {
          "return 400" in {
            given.commonPrecondition
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "companyName" -> Seq("")
              ))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include(pageTitle)
              page.getElementsByTag("h1").text() must include(pageHeading)
              page.getElementById("companyName-error").text() must include(emptyTextError)
            }
          }
        }

        "User enters over 140 characters" should {
          "return 400" in {
            given.commonPrecondition
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "companyName" -> Seq("ghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgf" +
                  "hghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhghfg" +
                  "dhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhs")
              ))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include(pageTitle)
              page.getElementsByTag("h1").text() must include(pageHeading)
              page.getElementById("companyName-error").text() must include(characterLimitError)
            }
          }
        }
      }
    }
  }
}