package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.mvc.Http.HeaderNames

class BusinessPartnersPartnershipVatNumberControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersPartnershipVatNumberController.load().url.drop(6)
  val partershipSaUtrUrl: String = routes.BusinessPartnersPartnershipUtrController.load().url
  val partnershipRegAddressUrl: String = routes.BusinessPartnerPartnershipRegisteredAddressController.load().url

  s"GET $route" should {

    "render the partnership-vat-registration-number page" in {
      given
        .commonPrecondition

      val result = buildRequest(route)
        .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
        .get()

      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.title() must include("Does the partner have a UK VAT registration number?")
        page.getElementsByTag("h1").text() must include("Does Test Partner have a UK VAT registration number?")
      }
    }
  }

  s"POST $route" when {
    "Yes is selected and Vat Number supplied, and legal entity type is Partnership" should {
      "redirect to the Partnership SA UTR page" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "partnership")
          )
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map(
            "vatNumber_yesNo" -> Seq("true"),
            "vatNumber_value" -> Seq("123456789")
          ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(partershipSaUtrUrl)
        }
      }
    }

    "No is selected, and legal entity type is Partnership" should {
      "redirect to the Partnership SA UTR page" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "partnership")
          )
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map("vatNumber_yesNo" -> Seq("false")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(partershipSaUtrUrl)
        }
      }
    }

    "Yes is selected and Vat Number supplied, and legal entity type is Limited Liability Partnership" should {
      "redirect to the Partnership Registered Office Address page" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "limited-liability-partnership")
          )
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(
            "vatNumber_yesNo" -> Seq("true"),
            "vatNumber_value" -> Seq("123456789")
          ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(partnershipRegAddressUrl)
        }
      }
    }

    "No is selected, and legal entity type is Limited Liability Partnership" should {
      "redirect to the Partnership SA UTR page" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "limited-liability-partnership")
          )
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map("vatNumber_yesNo" -> Seq("false")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(partershipSaUtrUrl)
        }
      }
    }

    "Radio button option not selected" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "limited-liability-partnership")
          )
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("vatNumber_yesNo" -> Seq.empty))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select whether the business has a VAT registration number")
        }
      }
    }

    "Yes is selected but VAT Number not supplied" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "limited-liability-partnership")
          )
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("vatNumber_yesNo" -> Seq("true")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter the VAT registration number")
        }
      }
    }

    "the form hasVat field is invalid format" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "limited-liability-partnership")
          )
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("vatNumber_yesNo" -> Seq("error")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select whether the business has a VAT registration number")
        }
      }
    }

    "the vatnumber field is invalid format" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("vatNumber_yesNo" -> Seq("true"),
            "vatNumber_value" -> Seq("1234")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter a valid UK VAT registration number")
        }
      }
    }
  }
}