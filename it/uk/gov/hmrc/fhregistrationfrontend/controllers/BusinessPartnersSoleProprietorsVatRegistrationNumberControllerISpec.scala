package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.models.VatNumber
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.EnterVatNumberPage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersSoleProprietorsVatRegistrationNumberControllerISpec
  extends Specifications with TestConfiguration {
    val index: Int = 1
    def route(mode: Mode): String = routes.BusinessPartnersSoleProprietorsVatRegistrationNumberController.load(index, mode).url.drop(6)

    def userAnswersWithPageData(formAnswers: VatNumber) = emptyUserAnswers
      .set[VatNumber](EnterVatNumberPage(1), formAnswers)
      .success
      .value

    val withVatRegNum = VatNumber(true, Some("123456789"))
    val withoutVatRegNum = VatNumber(false, None)

    List(NormalMode, CheckMode).foreach { mode =>


        val addressPage: String = routes.BusinessPartnersAddressController.load(index, mode).url
        val selfAssessmentUtrPage: String = routes.BusinessPartnersSoleProprietorUtrController.load(index, mode).url
        val businessPartnersPage: String = routes.BusinessPartnersController.load(index, mode).url

        s"GET ${route(mode)}" when {

            "render the partner-vat-registration-number page" when {
                "there are user answers but no form data" in {
                    given.commonPrecondition

                    addUserAnswersToSession(emptyUserAnswers)

                    val result = buildRequest(route(mode))
                      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                      .get()

                    whenReady(result) { res =>
                        res.status mustBe 200
                        val page = Jsoup.parse(res.body)
                        page.title() must include("Does the partner have a UK VAT registration number?")
                        page.getElementsByTag("h1").text() must include(
                            "Does test partner have a UK VAT registration number?"
                        )
                        val vatRegistrationNumberField = page.getElementById("vatNumber_value")
                        vatRegistrationNumberField.hasAttr("value") mustBe false
                    }
                }

                "there are user answers but no VAT Reg Num is provided" in {
                    given.commonPrecondition

                    addUserAnswersToSession(userAnswersWithPageData(withoutVatRegNum))

                    val result = buildRequest(route(mode))
                      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                      .get()

                    whenReady(result) { res =>
                        res.status mustBe 200
                        val page = Jsoup.parse(res.body)
                        page.title() must include("Does the partner have a UK VAT registration number?")
                        page.getElementsByTag("h1").text() must include(
                            "Does test partner have a UK VAT registration number?"
                        )
                        val vatRegistrationNumberField = page.getElementById("vatNumber_value")
                        vatRegistrationNumberField.hasAttr("value") mustBe false
                    }
                }

                "there are user answers and VAT Reg Num is provided" in {
                    given.commonPrecondition

                    addUserAnswersToSession(userAnswersWithPageData(withVatRegNum))

                    val result = buildRequest(route(mode))
                      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                      .get()

                    whenReady(result) { res =>
                        res.status mustBe 200
                        val page = Jsoup.parse(res.body)
                        page.title() must include("Does the partner have a UK VAT registration number?")
                        page.getElementsByTag("h1").text() must include(
                            "Does test partner have a UK VAT registration number?"
                        )
                        val vatRegistrationNumberField = page.getElementById("vatNumber_value")
                        vatRegistrationNumberField.attr("value") must include("123456789")
                    }
                }

                s"There are no user answers in the database in $mode" should {
                    "redirect to the start of the journey" in {
                        given.commonPrecondition

                        val result = buildRequest(route(mode))
                          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck").withFollowRedirects(false)
                          .post(Map(
                              "vatNumber_yesNo" -> Seq("true"),
                              "vatNumber_value" -> Seq("123456789")
                          ))

                        whenReady(result) { res =>
                            res.status mustBe 303
                            res.header(HeaderNames.LOCATION) mustBe Some(startCall.url)
                        }
                    }
                }
            }
        }

        s"POST ${route(mode)}" when {
            Map(
                "override" -> List(userAnswersWithPageData(withoutVatRegNum), userAnswersWithPageData(withVatRegNum)),
                "add" -> List(emptyUserAnswers)
            ).foreach { case (userAnswersAction, userAnswers) =>
                userAnswers.zipWithIndex.foreach { answers =>
                    s"redirect to the correct page and $userAnswersAction userAnswers ${answers._2}" when {
                        "yes is selected and the vatnumber entered" in {
                            given.commonPrecondition

                            addUserAnswersToSession(answers._1)

                            val result = buildRequest(route(mode))
                              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck").withFollowRedirects(false)
                              .post(Map(
                                  "vatNumber_yesNo" -> Seq("true"),
                                  "vatNumber_value" -> Seq("123456789")
                              ))

                            whenReady(result) { res =>
                                res.status mustBe 303
                                res.header(HeaderNames.LOCATION) mustBe Some(addressPage)
                            }
                        }

                        "no is selected" in {
                            given.commonPrecondition

                            addUserAnswersToSession(answers._1)

                            val result = buildRequest(route(mode))
                              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck").withFollowRedirects(false)
                              .post(Map("vatNumber_yesNo" -> Seq("false")))

                            whenReady(result) { res =>
                                res.status mustBe 303
                                res.header(HeaderNames.LOCATION) mustBe Some(selfAssessmentUtrPage)
                            }
                        }

                        "There are no user answers in the database" should {
                            "redirect to the start of the journey" in {
                                given.commonPrecondition

                                val result = buildRequest(route(mode))
                                  .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                                  .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck").withFollowRedirects(false)
                                  .post(Map(
                                      "vatNumber_yesNo" -> Seq("true"),
                                      "vatNumber_value" -> Seq("123456789")
                                  ))

                                whenReady(result) { res =>
                                    res.status mustBe 303
                                    res.header(HeaderNames.LOCATION) mustBe Some(startCall.url)
                                }
                            }
                        }
                    }
                }
            }

            "return 400" when {
                "hasVat number selected but vatNumber not supplied" in {
                    given.commonPrecondition

                    addUserAnswersToSession(emptyUserAnswers)

                    val result = buildRequest(route(mode))
                      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                      .withHttpHeaders(xSessionId,
                          "Csrf-Token" -> "nocheck")
                      .post(Map("vatNumber_yesNo" -> Seq("true")))

                    whenReady(result) { res =>
                        res.status mustBe 400
                        val page = Jsoup.parse(res.body)
                        page.getElementsByClass("govuk-error-summary").text() must include(
                            "There is a problem Enter the VAT registration number"
                        )
                    }
                }

                "the form hasVat field is invalid format" in {
                    given.commonPrecondition

                    addUserAnswersToSession(emptyUserAnswers)

                    val result = buildRequest(route(mode))
                      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                      .withHttpHeaders(xSessionId,
                          "Csrf-Token" -> "nocheck")
                      .post(Map("vatNumber_yesNo" -> Seq("error")))

                    whenReady(result) { res =>
                        res.status mustBe 400
                        val page = Jsoup.parse(res.body)
                        page.getElementsByClass("govuk-error-summary").text() must include(
                            "There is a problem Select whether the business has a VAT registration number"
                        )
                    }
                }

                "the vatnumber field is invalid format" in {
                    given.commonPrecondition

                    addUserAnswersToSession(emptyUserAnswers)

                    val result = buildRequest(route(mode))
                      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                      .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                      .post(Map("vatNumber_yesNo" -> Seq("true"), "vatNumber_value" -> Seq("1234")))

                    whenReady(result) { res =>
                        res.status mustBe 400
                        val page = Jsoup.parse(res.body)
                        page.getElementsByClass("govuk-error-summary").text() must include(
                            "There is a problem Enter a valid UK VAT registration number"
                        )
                    }
                }
            }
        }
    }
}
