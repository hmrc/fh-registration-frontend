package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerNinoControllerIntegrationSpec
  extends Specifications with TestConfiguration {


  "GET /form/business-partners/partner-national-insurance-number" when {

    "render the business partner national insurance number page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partner-national-insurance-number")
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

  "POST /form/business-partners/partner-national-insurance-number" when {

    "form with no errors" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/form/business-partners/partner-national-insurance-number")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "nationalInsuranceNumber_yesNo" -> Seq("true"),
                "nationalInsuranceNumber_value" -> Seq("QQ123456C")
              ))

            whenReady(result) { res =>
              res.status mustBe 200
            }
          }
        }

        "the user selects no" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/form/business-partners/partner-national-insurance-number")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "nationalInsuranceNumber_yesNo" -> Seq("false"),
                "nationalInsuranceNumber_value" -> Seq.empty
              ))

            whenReady(result) { res =>
              res.status mustBe 200
            }
          }
        }
      }
    }

    "no radio is selected by the user" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partner-national-insurance-number")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "nationalInsuranceNumber_yesNo" -> Seq.empty,
              "nationalInsuranceNumber_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
          }
        }
      }
    }

    "yes is selected but no NINO is entered" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partner-national-insurance-number")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "nationalInsuranceNumber_yesNo" -> Seq("true"),
              "nationalInsuranceNumber_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
          }
        }
      }
    }


  }

}
