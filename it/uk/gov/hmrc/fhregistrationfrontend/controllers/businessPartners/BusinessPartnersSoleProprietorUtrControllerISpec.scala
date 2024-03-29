package uk.gov.hmrc.fhregistrationfrontend.controllers.businessPartners

import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.models.TradingName
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.Utr
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.{SoleProprietorUtrPage, SoleProprietorsTradingNamePage}
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersSoleProprietorUtrControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode) = routes.BusinessPartnersSoleProprietorUtrController.load(index = 1, mode).url.drop(6)

  def userAnswersWithPageData(formAnswers: Utr) = emptyUserAnswers
    .set[Utr](SoleProprietorUtrPage(1), formAnswers)
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" when {

      "the user is authenticated" should {
        "render the partnership-self-assessment-unique-taxpayer-reference page" when {
          "there are user answers but no form data" in {
            given.commonPrecondition

            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("What is the partner’s Corporation Tax Unique Taxpayer Reference (UTR)?")
              page.getElementsByTag("h1").text() must include("What is {{partner name}}’s Self Assessment Unique Taxpayer Reference (UTR)?")
              val utrValueField = page.getElementById("uniqueTaxpayerReference_value")
              utrValueField.hasAttr("value") mustBe false
            }
          }

          "there are user answers no utr is given" in {
            given.commonPrecondition

            addUserAnswersToSession(userAnswersWithPageData(Utr("")))

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include("What is the partner’s Corporation Tax Unique Taxpayer Reference (UTR)?")
              page.getElementsByTag("h1").text must include("What is {{partner name}}’s Self Assessment Unique Taxpayer Reference (UTR)?")
              val utrValueField = page.getElementById("uniqueTaxpayerReference_value")
              utrValueField.hasAttr("value") mustBe false
            }
          }

            "there are user answers utr is given" in {
              given.commonPrecondition

              addUserAnswersToSession(userAnswersWithPageData(Utr("1234567890")))

              val result = buildRequest(route(mode))
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

              whenReady(result) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title must include("What is the partner’s Corporation Tax Unique Taxpayer Reference (UTR)?")
                page.getElementsByTag("h1").text must include("What is {{partner name}}’s Self Assessment Unique Taxpayer Reference (UTR)?")
                val utrValueField = page.getElementById("uniqueTaxpayerReference_value")
                utrValueField.hasAttr("value") mustBe true
              }
            }
          }

        "redirect the user to the start of the journey" when {
          "there is no user answers in the database" in {
            given.commonPrecondition
            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(startCall.url)
            }
          }
        }
      }

    }

    s"POST ${route(mode)}" when {
      "yes is selected and the UTR is entered" should {
        "redirect to the Partner Address page" in {
          given
            .commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)
          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("uniqueTaxpayerReference_value" -> Seq("1234567890")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersAddressController.load(1, mode).url)
          }
        }
      }

      "No Utr number is supplied" should {
        "return 400" in {
          given
            .commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)
          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("uniqueTaxpayerReference_value" -> Seq("")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter a Self Assessment Unique Taxpayer Reference (UTR)")
          }
        }
      }

      "the UTR field is invalid" should {
        "return 400" in {
          given
            .commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)
          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("uniqueTaxpayerReference_value" -> Seq("1234")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter a valid Self Assessment Unique Taxpayer Reference (UTR)")
          }
        }
      }
    }
  }
}