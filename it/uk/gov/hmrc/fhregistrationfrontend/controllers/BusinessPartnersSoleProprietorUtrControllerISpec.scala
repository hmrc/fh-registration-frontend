package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.NormalMode
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersSoleProprietorUtrControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersSoleProprietorUtrController.load().url.drop(6)

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
        page.title() must include("What is the partner’s Corporation Tax Unique Taxpayer Reference (UTR)?")
        page.getElementsByTag("h1").text() must include("What is {{partner name}}’s Self Assessment Unique Taxpayer Reference (UTR)?")
      }
    }
  }

  s"POST $route" when {
    "yes is selected and the UTR is entered" should {
      "redirect to the Partner Address page" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("uniqueTaxpayerReference_value" -> Seq("1234567890")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersAddressController.load(1, NormalMode).url)
        }
      }
    }

    "No Utr number is supplied" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
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

        val result = buildRequest(route)
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