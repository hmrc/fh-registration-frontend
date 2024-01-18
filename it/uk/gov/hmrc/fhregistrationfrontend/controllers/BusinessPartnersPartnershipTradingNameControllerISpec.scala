package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.models.TradingName
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnershipTradingNamePage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersPartnershipTradingNameControllerISpec extends Specifications with TestConfiguration {

  val noTradingName = TradingName(false, None)
  val tradingName = TradingName(true, Some("trading name"))
  val index = 1

  def route(mode: Mode) = routes.BusinessPartnersPartnershipTradingNameController.load(1, mode).url.drop(6)

  def userAnswersWithPageData(formAnswers: TradingName): UserAnswers =
    emptyUserAnswers
      .set[TradingName](PartnershipTradingNamePage(1), formAnswers)
      .success
      .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" when {
      "render the business partner partnership trading name page" when {
        "the are no userAnswers in the database" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(
              "Does the partnership use a trading name that is different from its registered name?")
            page.getElementsByTag("h1").text must include(
              "Does Test User use a trading name that is different from its registered name?")
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
              "Does the partnership use a trading name that is different from its registered name?")
            page.getElementsByTag("h1").text must include(
              "Does Test User use a trading name that is different from its registered name?")
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
              "Does the partnership use a trading name that is different from its registered name?")
            page.getElementsByTag("h1").text must include(
              "Does Test User use a trading name that is different from its registered name?")
            val tradingNameValueField = page.getElementById("tradingName_value")
            tradingNameValueField.attr("value") mustBe "trading name"
          }
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
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersController.load().url)
          }
        }
      }
    }

    s"POST ${route(mode)}" when {
      val partnershipVatRegNumUrl: String = routes.BusinessPartnersPartnershipVatNumberController.load().url
      val partnershipCompanyRegNumUrl: String = routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.load().url
      val businessPartnersUrl: String = routes.BusinessPartnersController.load().url

      "form with no errors" should {
        Map(
          "override" -> List(userAnswersWithPageData(noTradingName), userAnswersWithPageData(tradingName)),
          "add"      -> List(emptyUserAnswers)
        ).foreach {
          case (userAnswersAction, userAnswers) =>
            userAnswers.zipWithIndex.foreach { answers: (UserAnswers, Int) =>
              s"redirect to the Business Partners Partnership Vat Registration number page and $userAnswersAction userAnswers ${answers._2}" when {
                "businessType is partnership, the user selects yes and gives a trading name" in {
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
                    res.header(HeaderNames.LOCATION) mustBe Some(partnershipVatRegNumUrl)
                    val userAnswers = getUserAnswersFromSession.get
                    val pageData = userAnswers.get(PartnershipTradingNamePage(1))
                    pageData mustBe Some(TradingName(hasValue = true, Some("Blue Peter")))
                  }
                }

                "businessType is partnership, the user selects no and gives no trading name" in {
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
                        "tradingName_yesNo" -> Seq("false"),
                        "tradingName_value" -> Seq("")
                      ))

                  whenReady(result) { res =>
                    res.status mustBe 303
                    res.header(HeaderNames.LOCATION) mustBe Some(partnershipVatRegNumUrl)
                    val userAnswers = getUserAnswersFromSession.get
                    val pageData = userAnswers.get(PartnershipTradingNamePage(1))
                    pageData mustBe Some(TradingName(hasValue = false, None))
                  }
                }
              }

              s"redirect to the Business Partners Partnership Company Registration number page and $userAnswersAction userAnswers ${answers._2}" when {
                "businessType is partnership, the user selects yes and gives a trading name" in {
                  given.commonPrecondition
                  addUserAnswersToSession(answers._1)

                  val result = buildRequest(route(mode))
                    .addCookies(
                      DefaultWSCookie("mdtp", authAndSessionCookie),
                      DefaultWSCookie("businessType", "limited-liability-partnership")
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
                    res.header(HeaderNames.LOCATION) mustBe Some(partnershipCompanyRegNumUrl)
                    val userAnswers = getUserAnswersFromSession.get
                    val pageData = userAnswers.get(PartnershipTradingNamePage(1))
                    pageData mustBe Some(TradingName(hasValue = true, Some("Blue Peter")))
                  }
                }

                "businessType is partnership, the user selects no and gives no trading name" in {
                  given.commonPrecondition
                  addUserAnswersToSession(answers._1)

                  val result = buildRequest(route(mode))
                    .addCookies(
                      DefaultWSCookie("mdtp", authAndSessionCookie),
                      DefaultWSCookie("businessType", "limited-liability-partnership")
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
                    res.header(HeaderNames.LOCATION) mustBe Some(partnershipCompanyRegNumUrl)
                    val userAnswers = getUserAnswersFromSession.get
                    val pageData = userAnswers.get(PartnershipTradingNamePage(1))
                    pageData mustBe Some(TradingName(hasValue = false, None))
                  }
                }
              }
            }
        }

        s"redirect to the start of Business Partners journey" when {
          "businessType is neither partnership or LLP" in {
            given.commonPrecondition
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(
                DefaultWSCookie("mdtp", authAndSessionCookie),
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
              res.header(HeaderNames.LOCATION) mustBe Some(businessPartnersUrl)
              val userAnswers = getUserAnswersFromSession.get
              val pageData = userAnswers.get(PartnershipTradingNamePage(1))
              pageData mustBe Some(TradingName(hasValue = false, None))
            }
          }
        }
      }

      "form with errors" should {
        "return 400 bad request " when {
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
                "Does the partnership use a trading name that is different from its registered name?")
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
                "Does the partnership use a trading name that is different from its registered name?")
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
                "Enter the trading name")
            }
          }

        }

      }
    }
  }
}
