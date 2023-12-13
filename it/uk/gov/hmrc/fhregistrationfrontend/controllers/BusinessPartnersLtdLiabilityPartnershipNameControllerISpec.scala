package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.DefaultWSCookie
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersLtdLiabilityPartnershipNameControllerISpec
  extends Specifications with TestConfiguration {

  val route = routes.BusinessPartnersLtdLiabilityPartnershipNameController.load().url.drop(6)

  val pageTitle: String = "What is the name of the limited liability partnership?"
  val pageHeading: String = "What is the name of the limited liability partnership?"
  val tradingNamePageUrl: String = routes.BusinessPartnerPartnershipTradingNameController.load().url
  val characterLimitError: String = "There is a problem Limited liability partnership name must be 120 characters or less"
  val emptyTextError: String = "There is a problem Enter the name of the limited liability partnership"

  s"GET $route" should {
    "render the Limited Liability Partnership Name page" in {
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
    "the limited liability partnership name is entered" should {
      "return 303 with ltdLiabilityPartnershipName" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("ltdLiabilityPartnershipName" -> Seq("Partnership Name")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(tradingNamePageUrl)
          }
      }
    }

    "Limited liability partnership name is not supplied" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("ltdLiabilityPartnershipName" -> Seq("")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include(emptyTextError)
          }
      }
    }

    "Limited liability partnership name is not valid (exceeds 120 chars)" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("ltdLiabilityPartnershipName" -> Seq("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include(characterLimitError)
          }
      }
    }
  }
}