package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerUtrControllerIntegrationSpec
  extends Specifications with TestConfiguration {


  "GET /form/business-partners/partnership-self-assessment-unique-taxpayer-reference" when {

    "render the business partner Unique Taxpayer Reference page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partnership-self-assessment-unique-taxpayer-reference")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("Does the partner have a Self Assessment Unique Taxpayer Reference (UTR)?")
            page.getElementById("page-heading").text must include("Does Test User have a Self Assessment Unique Taxpayer Reference (UTR)?")
          }
        }
      }
    }

  }

  "POST /form/business-partners/partnership-self-assessment-unique-taxpayer-reference" when {

    "form with no errors" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/form/business-partners/partnership-self-assessment-unique-taxpayer-reference")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "uniqueTaxpayerReference_yesNo" -> Seq("true"),
                "uniqueTaxpayerReference_value" -> Seq("1234567890")
              ))

            whenReady(result) { res =>
              res.status mustBe 200
            }
          }
        }

        "the user selects no" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/form/business-partners/partnership-self-assessment-unique-taxpayer-reference")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "uniqueTaxpayerReference_yesNo" -> Seq("false"),
                "uniqueTaxpayerReference_value" -> Seq.empty
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
          val result = client.url(s"$baseUrl/form/business-partners/partnership-self-assessment-unique-taxpayer-reference")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "uniqueTaxpayerReference_yesNo" -> Seq.empty,
              "uniqueTaxpayerReference_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
          }
        }
      }
    }

    "yes is selected but no UTR is entered" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partnership-self-assessment-unique-taxpayer-reference")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "uniqueTaxpayerReference_yesNo" -> Seq("true"),
              "uniqueTaxpayerReference_value" -> Seq.empty
            ))

          whenReady(result) { res =>
            res.status mustBe 400
          }
        }
      }
    }


  }

}
