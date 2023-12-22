package uk.gov.hmrc.fhregistrationfrontend.controllers

import models._
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.models.NationalInsuranceNumber
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.IndividualsAndSoleProprietorsNinoPage
import org.scalatest.TryValues.convertTryToSuccessOrFailure

class BusinessPartnersIndividualsAndSoleProprietorsNinoControllerISpec
  extends Specifications with TestConfiguration {

  val index = 1

  def route(mode: Mode): String = routes.BusinessPartnersIndividualsAndSoleProprietorsNinoController.load(index, mode).url.drop(6)

  val nino = NationalInsuranceNumber(hasValue = true, Some("AB123456C"))
  val userAnswersWithPageData: UserAnswers = emptyUserAnswers
    .set[NationalInsuranceNumber](IndividualsAndSoleProprietorsNinoPage(1), nino)
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>

    s"GET ${route(mode)}" when {

      "render the business partner national insurance number page with no answers prepopulated" when {
        "the user is authenticated" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)

            val ninoField = page.getElementById("nationalInsuranceNumber_value")
            ninoField.attr("value") mustBe ""
            page.title must include("Does the partner have a National Insurance number?")
            page.getElementById("page-heading").text must include("Does Test User have a National Insurance number?")
          }
        }
      }

      "render the business partner national insurance number page with answers prepopulated" when {
        "the user is authenticated" in {
          given.commonPrecondition
          addUserAnswersToSession(userAnswersWithPageData)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)

            val ninoField = page.getElementById("nationalInsuranceNumber_value")
            ninoField.attr("value") mustBe "AB123456C"
            page.title must include("Does the partner have a National Insurance number?")
            page.getElementById("page-heading").text must include("Does Test User have a National Insurance number?")
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
      "the business type is Individual and the form is filled out correctly (Nino == true && AB123456C)" should {
        "redirect to the VAT number page" when {
          Map("override" -> userAnswersWithPageData, "add" -> emptyUserAnswers).foreach { case (uaAction, userAnswers) =>
            s"redirect to Business Partner NINO page and $uaAction userAnswers" when {
              "business type is Individual and the form is filled out correctly" in {
                given.commonPrecondition
                addUserAnswersToSession(userAnswers)

                val result = buildRequest(route(mode))
                  .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie), DefaultWSCookie("businessType", "individual"))
                  .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                  .post(Map(
                    "nationalInsuranceNumber_yesNo" -> Seq("true"),
                    "nationalInsuranceNumber_value" -> Seq("AB123456C")
                  ))

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnerAddressController.load().url)
                  val userAnswers = getUserAnswersFromSession.get
                  val pageData = userAnswers.get(IndividualsAndSoleProprietorsNinoPage(1))
                  pageData mustBe Some(NationalInsuranceNumber(hasValue = true, Some("AB123456C")))
                }
              }
            }
          }
        }
      }

      "the business type is Individual and the form is filled out correctly (Nino == false)" should {
        "redirect to the VAT number page" when {
          Map("override" -> userAnswersWithPageData, "add" -> emptyUserAnswers).foreach { case (uaAction, userAnswers) =>
            s"redirect to Business Partner NINO page and $uaAction userAnswers" when {
              "business type is Individual and the form is filled out correctly" in {
                given.commonPrecondition
                addUserAnswersToSession(userAnswers)

                val result = buildRequest(route(mode))
                  .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie), DefaultWSCookie("businessType", "individual"))
                  .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                  .post(Map(
                    "nationalInsuranceNumber_yesNo" -> Seq("false"),
                    "nationalInsuranceNumber_value" -> Seq("")
                  ))

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersVatRegistrationNumberController.load().url)
                  val userAnswers = getUserAnswersFromSession.get
                  val pageData = userAnswers.get(IndividualsAndSoleProprietorsNinoPage(1))
                  pageData mustBe Some(NationalInsuranceNumber(hasValue = false, None))
                }
              }
            }
          }
        }
      }

      "the business type is Individual and neither radio button is selected by the user" should {
        "redirect to the VAT number page" when {
          Map("override" -> userAnswersWithPageData, "add" -> emptyUserAnswers).foreach { case (uaAction, userAnswers) =>
            s"redirect to Business Partner NINO page and $uaAction userAnswers" when {
              "business type is Individual and the form is filled out correctly" in {
                given.commonPrecondition
                addUserAnswersToSession(emptyUserAnswers)

                val result = buildRequest(route(mode))
                  .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie), DefaultWSCookie("businessType", "individual"))
                  .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                  .post(Map(
                    "nationalInsuranceNumber_yesNo" -> Seq.empty,
                    "nationalInsuranceNumber_value" -> Seq.empty
                  ))

                whenReady(result) { res =>
                  res.status mustBe 400
                  val page = Jsoup.parse(res.body)
                  page.getElementsByClass("govuk-error-summary").text() must include("Select whether they have a National Insurance number")
                }
              }
            }
          }
        }
      }

      "the business type is Individual and yes is selected but no NINO is entered" should {
        "redirect to the VAT number page" when {
          Map("override" -> userAnswersWithPageData, "add" -> emptyUserAnswers).foreach { case (uaAction, userAnswers) =>
            s"redirect to Business Partner NINO page and $uaAction userAnswers" when {
              "business type is Individual and the form is filled out correctly" in {
                given.commonPrecondition
                addUserAnswersToSession(emptyUserAnswers)

                val result = buildRequest(route(mode))
                  .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie), DefaultWSCookie("businessType", "individual"))
                  .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                  .post(Map(
                    "nationalInsuranceNumber_yesNo" -> Seq("true"),
                    "nationalInsuranceNumber_value" -> Seq.empty
                  ))

                whenReady(result) { res =>
                  res.status mustBe 400
                  val page = Jsoup.parse(res.body)
                  page.getElementsByClass("govuk-error-summary").text() must include("Enter National Insurance number")
                }
              }
            }
          }
        }
      }
    }
  }
}
