package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.models.TradingName
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.CorporateBodyTradingNamePage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersCorporateBodyTradingNameControllerISpec extends Specifications with TestConfiguration {

  val noTradingName: TradingName = TradingName(false, None)
  val tradingName: TradingName = TradingName(true, Some("trading name"))
  val index = 1

  def route(mode: Mode): String =
    routes.BusinessPartnersCorporateBodyTradingNameController.load(index = 1, mode).url.drop(6)

  def userAnswersWithPageData(formAnswers: TradingName): UserAnswers =
    emptyUserAnswers
      .set[TradingName](CorporateBodyTradingNamePage(1), formAnswers)
      .success
      .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" when {

      "render the business partner corporate body trading name page" when {
        "there are user answers but no form data" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(
              "Does the corporate body use a trading name that is different from its registered name?")
            page.getElementsByTag("h1").text must include(
              "Does Shelby Limited use a trading name that is different from its registered name?")
          }
        }

        "userAnswers are stored in database (No trading name)" in {
          given.commonPrecondition
          addUserAnswersToSession(userAnswersWithPageData(noTradingName))

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(
              "Does the corporate body use a trading name that is different from its registered name?")
            page.getElementsByTag("h1").text must include(
              "Does Shelby Limited use a trading name that is different from its registered name?")
            val tradingNameValueField = page.getElementById("tradingName_value")
            tradingNameValueField.hasAttr("value") mustBe false
          }
        }

        "userAnswers are stored in database (yes with trading name)" in {
          given.commonPrecondition
          addUserAnswersToSession(userAnswersWithPageData(tradingName))

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(
              "Does the corporate body use a trading name that is different from its registered name?")
            page.getElementsByTag("h1").text must include(
              "Does Shelby Limited use a trading name that is different from its registered name?")
            val tradingNameValueField = page.getElementById("tradingName_value")
            tradingNameValueField.attr("value") mustBe "trading name"
          }
        }
      }

      "redirect the user to the start of the journey" when {
        "there is no user answers in the database" in {
          given.commonPrecondition
          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(startCall.url)
          }
        }
      }
    }

    s"POST ${route(mode)}" when {
      val businessPartnerCorporateBodyRegistrationNumber: String =
        routes.BusinessPartnersCorporateBodyCompanyRegistrationNumberController.load().url

      "form with no errors" should {
        Map(
          "override" -> List(userAnswersWithPageData(noTradingName), userAnswersWithPageData(tradingName)),
          "add"      -> List(emptyUserAnswers)
        ).foreach {
          case (userAnswersAction, userAnswers) =>
            userAnswers.zipWithIndex.foreach { answers: (UserAnswers, Int) =>
              s"redirect to the Business Partners CorporateBody Company Registration Number page and $userAnswersAction userAnswers ${answers._2}" when {
                "the user selects yes and gives a trading name" in {
                  given.commonPrecondition
                  addUserAnswersToSession(answers._1)

                  val result = buildRequest(route(mode))
                    .addCookies(
                      DefaultWSCookie("mdtp", authAndSessionCookie),
                      DefaultWSCookie("businessType", "partnership")
                    )
                    .withHttpHeaders(
                      xSessionId,
                      "Csrf-Token" -> "nocheck"
                    )
                    .post(
                      Map(
                        "tradingName_yesNo" -> Seq("true"),
                        "tradingName_value" -> Seq("Blue Peter")
                      ))

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(businessPartnerCorporateBodyRegistrationNumber)
                    val userAnswers = getUserAnswersFromSession.get
                    val pageData = userAnswers.get(CorporateBodyTradingNamePage(1))
                    pageData mustBe Some(TradingName(hasValue = true, Some("Blue Peter")))
                  }
                }

                "the user selects no and gives no trading name" in {
                  given.commonPrecondition
                  addUserAnswersToSession(answers._1)

                  val result = buildRequest(route(mode))
                    .addCookies(
                      DefaultWSCookie("mdtp", authAndSessionCookie)
                    )
                    .withHttpHeaders(
                      xSessionId,
                      "Csrf-Token" -> "nocheck"
                    )
                    .post(
                      Map(
                        "tradingName_yesNo" -> Seq("false"),
                        "tradingName_value" -> Seq("")
                      ))

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(businessPartnerCorporateBodyRegistrationNumber)
                    val userAnswers = getUserAnswersFromSession.get
                    val pageData = userAnswers.get(CorporateBodyTradingNamePage(1))
                    pageData mustBe Some(TradingName(hasValue = false, None))
                  }
                }
              }
            }
        }
      }

      "form with errors" should {
        "return a 400 bad request" when {
          "no radio option is selected by the user" in {
            given.commonPrecondition
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "tradingName_yesNo" -> Seq.empty,
                  "tradingName_value" -> Seq.empty
                ))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include(
                "Does the corporate body use a trading name that is different from its registered name?")
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
                "Select whether the business has a different trading name")
            }
          }

          "yes radio option is selected by the user but no trading name supplied" in {
            given.commonPrecondition
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "tradingName_yesNo" -> Seq("true"),
                  "tradingName_value" -> Seq.empty
                ))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include(
                "Does the corporate body use a trading name that is different from its registered name?")
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
                "Enter the trading name")
            }
          }
        }
      }
    }
  }
}
