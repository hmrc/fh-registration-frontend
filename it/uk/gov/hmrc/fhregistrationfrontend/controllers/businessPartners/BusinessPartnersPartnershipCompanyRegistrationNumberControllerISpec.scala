package uk.gov.hmrc.fhregistrationfrontend.controllers.businessPartners

import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.models.CompanyRegistrationNumber
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.CompanyRegistrationNumberPage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersPartnershipCompanyRegistrationNumberControllerISpec
    extends Specifications with TestConfiguration {

  def route(mode: Mode): String =
    routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.load(1, mode).url.drop(6)
  def partnershipVatNumUrl(mode: Mode): String = routes.BusinessPartnersPartnershipVatNumberController.load(1, mode).url
  val userAnswersWithPageData: UserAnswers = emptyUserAnswers
    .set[CompanyRegistrationNumber](CompanyRegistrationNumberPage(1), CompanyRegistrationNumber("01234567", None))
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" when {
      "render the partnership-company-registration-number page" when {
        "there are user answers but no form data" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "limited-liability-partnership")
            )
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include("What is the partnership’s company registration number?")
            page.getElementsByTag("h1").text() must include("What is Test Partnership’s company registration number?")
          }
        }

        "there are user answers with form data" in {
          given.commonPrecondition
          addUserAnswersToSession(userAnswersWithPageData)

          val result = buildRequest(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "limited-liability-partnership")
            )
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include("What is the partnership’s company registration number?")
            page.getElementsByTag("h1").text() must include("What is Test Partnership’s company registration number?")
            val companyRegistrationNumber = page.getElementById("companyRegistrationNumber")
            companyRegistrationNumber.attr("value") mustBe "01234567"
          }
        }
      }

      "there is no user answers in the database" should {
        "redirect to the start" in {
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
      "the companyRegistrationNumber is entered" should {
        "redirect to the Partnership VAT Registration Number page" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "limited-liability-partnership")
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("companyRegistrationNumber" -> Seq("01234567")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(partnershipVatNumUrl(mode))
          }
        }

        "redirect to the Partnership VAT Registration Number page (letter formatted companyRegistrationNumber)" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "limited-liability-partnership")
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("companyRegistrationNumber" -> Seq("AB123456")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(partnershipVatNumUrl(mode))
          }
        }
      }

      "companyRegistrationNumber is not supplied" should {
        "return 400" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "limited-liability-partnership")
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("companyRegistrationNumber" -> Seq("")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include(
              "There is a problem Enter the company registration number")
          }
        }
      }

      "the companyRegistrationNumber field is invalid format" should {
        "return 400" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "limited-liability-partnership")
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("companyRegistrationNumber" -> Seq("aaa")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include(
              "There is a problem Company registration number must be 8 numbers, or 2 letters followed by 6 numbers")
          }
        }
      }
    }
  }
}
