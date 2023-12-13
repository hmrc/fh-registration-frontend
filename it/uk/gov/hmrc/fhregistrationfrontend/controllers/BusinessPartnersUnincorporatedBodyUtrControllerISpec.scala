package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.mvc.Http.HeaderNames

class BusinessPartnersUnincorporatedBodyUtrControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersUnincorporatedBodyUtrController.load().url.drop(6)
  val registeredOfficeAddressPageUrl: String = routes.BusinessPartnersUnincorporatedBodyRegisteredAddressController.load().url
  val pageHeading = "Does {{Unincorporated body name}} have a Self Assessment Unique Taxpayer Reference (UTR)?"
  val pageTitle = "Does the partner have a Self Assessment Unique Taxpayer Reference (UTR)?"

  val noSelectionError = "There is a problem Select whether they have a Self Assessment Unique Taxpayer Reference"
  val blankFieldError = "There is a problem Enter a Self Assessment Unique Taxpayer Reference (UTR)"
  val utrFormatError = "There is a problem Enter a valid Self Assessment Unique Taxpayer Reference (UTR)"

  s"GET $route" should {

    "render the partnership-self-assessment-unique-taxpayer-reference page" in {
      given
        .commonPrecondition

      val result = buildRequest(route)
        .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
        .get()

      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.title() must include(pageTitle)
        page.getElementsByTag("h1").text() must include(pageHeading)
      }
    }
  }

  s"POST $route" when {
    "yes is selected and the UTR is entered" should {
      "redirect to the Unincorporated Body Registered Office Address page" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map(
            "uniqueTaxpayerReference_yesNo" -> Seq("true"),
            "uniqueTaxpayerReference_value" -> Seq("1234567890")
          ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(registeredOfficeAddressPageUrl)
        }
      }
    }

    "no is selected" should {
      "redirect to the Unincorporated Body Registered Office Address page" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("uniqueTaxpayerReference_yesNo" -> Seq("false")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(registeredOfficeAddressPageUrl)
        }
      }
    }

    "no option selected" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
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
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
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
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("uniqueTaxpayerReference_yesNo" -> Seq("true"),
            "uniqueTaxpayerReference_value" -> Seq("1234")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include(utrFormatError)
        }
      }
    }
  }
}