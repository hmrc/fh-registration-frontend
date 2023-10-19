package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.mvc.Http.HeaderNames
import scala.collection.immutable.Seq

class BusinessPartnerNinoControllerIntegrationSpec
  extends Specifications with TestConfiguration {


  "GET /business-partners/partner-national-insurance-number" when {

    "render the business partner national insurance number page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/business-partners/partner-national-insurance-number")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("Does the partner have a National Insurance number?")

            page.getElementById("page-heading").text must include("Does Test User have a National Insurance number?")
          }
        }
      }
    }

  }

  "POST /business-partners/partner-national-insurance-number" when {
    "the Yes radio button is selected and a valid NINO is provided (for Individual)" should {
      "redirect to the VAT number page" in {
          given.commonPrecondition

          val result = buildRequest(s"/business-partners/partner-national-insurance-number")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "nationalInsuranceNumber_yesNo" -> Seq("true"),
                "nationalInsuranceNumber_value" -> Seq("AB123456C")
              ))

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/business-partners/partner-address")
            }
          }
        }

        "the Yes radio button is selected and a valid NINO is provided (for Sole Proprietor)" should {
          "redirect to the VAT number page" in {
            given.commonPrecondition

            val result = buildRequest(s"/business-partners/partner-national-insurance-number")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "nationalInsuranceNumber_yesNo" -> Seq("true"),
                "nationalInsuranceNumber_value" -> Seq("QQ456789C")
              ))

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/partner-vat-registration-number")
            }
          }
        }

        "the No radio button is selected" should {
          "redirect to the VAT number page" in {
            given.commonPrecondition

            val result = buildRequest(s"/business-partners/partner-national-insurance-number")
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                .post(Map(
                  "nationalInsuranceNumber_yesNo" -> Seq("false"),
                  "nationalInsuranceNumber_value" -> Seq.empty
                ))

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/form/business-partners/partner-vat-registration-number")
            }
          }
        }
      }

    "neither radio button is selected by the user" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/business-partners/partner-national-insurance-number")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
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

    "yes is selected but no NINO is entered" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/business-partners/partner-national-insurance-number")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
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
