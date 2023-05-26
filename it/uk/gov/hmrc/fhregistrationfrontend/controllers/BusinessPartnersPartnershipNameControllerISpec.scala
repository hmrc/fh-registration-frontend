package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersPartnershipNameControllerISpec
  extends Specifications with TestConfiguration {

  val requestUrl = "form/business-partners/partnership-name"


  "GET /form/business-partners/partnership-name" when {

    "render the business partners partnership name page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("What is the name of the partnership? - Business partners")
            page.getElementsByTag("h1").text must include("What is the name of the partnership?")
          }
        }
      }
    }

  }

  "POST /form/business-partners/partnership-name" when {

    "the user submits with a company name" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "partnershipName" -> Seq("Shelby Limited")
              ))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Form submitted, with result: PartnershipNameModel(Shelby Limited)")
            }
          }
        }
      }
    }

    "User does not enter a partnership name" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "partnershipName" -> Seq("")
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("What is the name of the partnership? - Business partners")
            page.getElementsByTag("h1").text() must include("What is the name of the partnership?")
            page.getElementById("partnershipName-error").text() must include("Enter a partnership name")
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
              "companyName" -> Seq("ghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgf" +
                "hghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhghfg" +
                "dhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhghfgdhdgfhfgfhs")
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("What is the name of the partnership? - Business partners")
            page.getElementsByTag("h1").text() must include("What is the name of the partnership?")
            page.getElementById("partnershipName-error").text() must include("Enter a partnership name")
          }
        }
      }
    }
  }
}