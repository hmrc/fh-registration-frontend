package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.models.HasUniqueTaxpayerReference
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnershipHasUtrPage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersPartnershipUtrControllerISpec extends Specifications with TestConfiguration {

  val route: Mode => String = mode => routes.BusinessPartnersPartnershipUtrController.load(1, mode).url.drop(6)
  val pageHeading = "Does test partner have a Self Assessment Unique Taxpayer Reference (UTR)?"
  val pageTitle = "Does the partner have a Self Assessment Unique Taxpayer Reference (UTR)?"

  val noSelectionError = "There is a problem Select whether they have a Self Assessment Unique Taxpayer Reference"
  val blankFieldError = "There is a problem Enter a Self Assessment Unique Taxpayer Reference (UTR)"
  val utrFormatError = "There is a problem Enter a valid Self Assessment Unique Taxpayer Reference (UTR)"

  val yesWithUtr: HasUniqueTaxpayerReference = HasUniqueTaxpayerReference(hasValue = true, Some("1234567890"))
  val noWithoutUtr: HasUniqueTaxpayerReference = HasUniqueTaxpayerReference(hasValue = false, None)

  def userAnswersWithPageData(answers: HasUniqueTaxpayerReference): UserAnswers =
    emptyUserAnswers
      .set[HasUniqueTaxpayerReference](PartnershipHasUtrPage(1), answers)
      .success
      .value

  List(NormalMode, CheckMode).foreach { mode =>
    def registeredOfficeAddressPageUrl(mode: Mode): String =
      routes.BusinessPartnersPartnershipRegisteredAddressController.load(1, mode).url

    s"GET ${route(mode)}" should {
      "render the Partnership has utr page with no prepopulated answers" in {
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
        }
      }

      "render the Partnership has utr page with answers prepopulated (yes with Utr)" in {
        given.commonPrecondition
        addUserAnswersToSession(userAnswersWithPageData(yesWithUtr))

        val result = buildRequest(route(mode))
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include(pageTitle)

          page.getElementsByTag("h1").text() must include(pageHeading)
          val form = page.getElementById("uniqueTaxpayerReference_value")
          form.attr("value") mustBe "1234567890"
          val yesRadioButton = page.getElementById("uniqueTaxpayerReference_yesNo")
          yesRadioButton.hasAttr("checked") mustBe true
        }
      }

      "render the Partnership has utr page with answers prepopulated (no without Utr)" in {
        given.commonPrecondition
        addUserAnswersToSession(userAnswersWithPageData(noWithoutUtr))

        val result = buildRequest(route(mode))
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include(pageTitle)

          page.getElementsByTag("h1").text() must include(pageHeading)
          val noRadioButton = page.getElementById("uniqueTaxpayerReference_yesNo-2")
          noRadioButton.hasAttr("checked") mustBe true
        }
      }

      "redirect to start of the journey if there are no user answers" in {
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

    s"POST ${route(mode)}" when {
      "yes is selected and the UTR is entered" should {
        "Redirect to registered office address page" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(
              Map("uniqueTaxpayerReference_yesNo" -> Seq("true"), "uniqueTaxpayerReference_value" -> Seq("1234567890")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(registeredOfficeAddressPageUrl(mode))
          }
        }
      }

      "no is selected" should {
        "Redirect to registered office address page" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("uniqueTaxpayerReference_yesNo" -> Seq("false"), "uniqueTaxpayerReference_value" -> Seq.empty))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(registeredOfficeAddressPageUrl(mode))
          }
        }
      }

      "no option selected" should {
        "return 400" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("uniqueTaxpayerReference_yesNo" -> Seq.empty))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include(noSelectionError)
          }
        }
      }

      "option selected but UTR not supplied" should {
        "return 400" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("uniqueTaxpayerReference_yesNo" -> Seq("true")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include(blankFieldError)
          }
        }
      }

      "the UTR field is invalid format" should {
        "return 400" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("uniqueTaxpayerReference_yesNo" -> Seq("true"), "uniqueTaxpayerReference_value" -> Seq("1234")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include(utrFormatError)
          }
        }
      }
    }
  }

}
