package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersCorporateBodyCompanyNameControllerISpec
  extends Specifications with TestConfiguration {

  val requestUrl = "form/business-partners/company-name"


  "GET /form/business-partners/company-name" when {

    "render the business partner unincorporated body trading name page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("What is the company name? - Business partners")
            page.getElementsByTag("h1").text must include("What is the company name?")
          }
        }
      }
    }

  }

  "POST /form/business-partners/unincorporated-body-trading-name" when {

    "the user selects yes and enters a trading name" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "companyName" -> Seq("Shelby Limited")
              ))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Form submitted, with result: companyNameModel(Shelby Limited)")
            }
          }
        }
      }
    }

    "User does not enter a company name" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "companyName" -> Seq("")
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("What is the company name? - Business partners")
            page.getElementsByTag("h1").text() must include("What is the company name?")
            page.getElementById("companyName-error").text() must include("Enter a valid company name")
          }
        }
      }
    }

    "User enters over 140 characters" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "companyName" -> Seq("ghfgdhdgfhfgfhghf" +
                "gdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhg" +
                "hfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhghfg" +
                "dhdgfhfgfhghfgdhdgfhfgfhs")
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("What is the company name? - Business partners")
            page.getElementsByTag("h1").text() must include("What is the company name?")
            page.getElementById("companyName-error").text() must include("Company Name must be less than 140 characters")
          }
        }
      }
    }
  }
}