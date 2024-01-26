package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import models._
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.TradingNameForm.{hasTradingNameKey, tradingNameKey}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.TradingName
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.SoleProprietorsTradingNamePage

class BusinessPartnersSoleProprietorsTradingNameControllerISpec
  extends Specifications with TestConfiguration {

  def ninoPage(mode: Mode) = routes.BusinessPartnersIndividualsAndSoleProprietorsNinoController.load(1, mode).url
  val noTradingName = TradingName(false, None)
  val tradingName = TradingName(true, Some("trading name"))

  def route(mode: Mode) = routes.BusinessPartnersSoleProprietorsTradingNameController.load(1, mode).url.drop(6)

  def userAnswersWithPageData(formAnswers: TradingName) = emptyUserAnswers
    .set[TradingName](SoleProprietorsTradingNamePage(1), formAnswers)
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" when {

      "the user is authenticated" should {
        "render the business partner trading name page" when {

          "there are user answers but no form data" in {
            given.commonPrecondition

            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include("Does the partner’s business use a trading name that is different from its registered name?")
              page.getElementsByTag("h1").text must include("Does Test User’s business use a trading name that is different from its registered name?")
              val tradingNameValueField = page.getElementById("tradingName_value")
              tradingNameValueField.hasAttr("value") mustBe false
            }
          }

          "there are user answers but no trading name is given" in {
            given.commonPrecondition

            addUserAnswersToSession(userAnswersWithPageData(noTradingName))

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include("Does the partner’s business use a trading name that is different from its registered name?")
              page.getElementsByTag("h1").text must include("Does Test User’s business use a trading name that is different from its registered name?")
              val tradingNameValueField = page.getElementById("tradingName_value")
              tradingNameValueField.hasAttr("value") mustBe false
            }
          }

          "there are user answers trading name is given" in {
            given.commonPrecondition

            addUserAnswersToSession(userAnswersWithPageData(tradingName))

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include("Does the partner’s business use a trading name that is different from its registered name?")
              page.getElementsByTag("h1").text must include("Does Test User’s business use a trading name that is different from its registered name?")
              val tradingNameValueField = page.getElementById("tradingName_value")
              tradingNameValueField.attr("value") mustBe "trading name"
            }
          }

        }

        "redirect the user to the start of the BusinessPartners journey" when {
          "there is no user answers in the database" in {
            given.commonPrecondition
            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersController.load(1, mode).url)
            }
          }
        }
      }

    }

    s"POST ${route(mode)}" when {
      "form with no errors" should {
        Map(
          "override" -> List(userAnswersWithPageData(noTradingName), userAnswersWithPageData(tradingName)),
          "add" -> List(emptyUserAnswers)
        ).foreach { case (userAnswersAction, userAnswers) =>
          userAnswers.zipWithIndex.foreach { answers: (UserAnswers, Int) =>
            s"redirect to the Business Partners National Insurance Number page and $userAnswersAction userAnswers ${answers._2}" when {
              "the user selects yes and gives a trading name" in {
                given.commonPrecondition

                addUserAnswersToSession(answers._1)

                val result = buildRequest(route(mode))
                  .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                  .withHttpHeaders(
                    xSessionId,
                    "Csrf-Token" -> "nocheck"
                  )
                  .post(Map(
                    "tradingName_yesNo" -> Seq("true"),
                    "tradingName_value" -> Seq("Blue Peter")
                  ))

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(ninoPage(mode))
                  val userAnswers = getUserAnswersFromSession.get
                  val pageData = userAnswers.get(SoleProprietorsTradingNamePage(1))
                  pageData mustBe Some(TradingName(true, Some("Blue Peter")))
                }
              }

              "the user selects no" in {
                given.commonPrecondition

                addUserAnswersToSession(answers._1)

                val result = buildRequest(route(mode))
                  .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                  .withHttpHeaders(
                    xSessionId,
                    "Csrf-Token" -> "nocheck"
                  )
                  .post(Map(
                    "tradingName_yesNo" -> Seq("false"),
                    "tradingName_value" -> Seq.empty
                  ))

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(ninoPage(mode))
                  val userAnswers = getUserAnswersFromSession.get
                  val pageData = userAnswers.get(SoleProprietorsTradingNamePage(1))
                  pageData mustBe Some(TradingName(false, None))
                }
              }
            }
          }
        }
      }

      "return 400" when {
        "no radio option is selected by the user" in {
          given.commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "tradingName_yesNo" -> Seq.empty,
              "tradingName_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Select whether the business has a different trading name")
          }
        }

        "yes is selected but no trading name is entered" in {
          given.commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "tradingName_yesNo" -> Seq("true"),
              "tradingName_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the trading name")
          }
        }
      }

      "there is no user answers in the database" should {
        "redirect to the start of BusinessPartners" in {
          given.commonPrecondition
          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(hasTradingNameKey -> Seq("true"), tradingNameKey -> Seq("some trading name")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersController.load(1, mode).url)
          }
        }
      }
    }
  }
}
