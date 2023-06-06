package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class MainBusinessAddressControllerISpec
  extends Specifications with TestConfiguration {
  val requestUrl = "mainBusinessAddress"
  val pageTitle = "Business address - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK"
  val pageHeading = "Business address"
  val errorMessage = "Select how long the business has operated from this address"

  "GET /mainBusinessAddress" when {

    "render the main business address" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(pageTitle)
            page.getElementsByTag("h1").text must include(pageHeading)
          }
        }
      }
    }

  }

  "POST /mainBusinessAddress" when {

    "the user selects 3-5 years and submits the form" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "timeAtCurrentAddress" -> Seq("3-5 years")
              ))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Form submitted, with result:MainBusinessAddress(3-5 years,None,None,None)")
            }
          }
        }
      }
    }

    "no radio option is selected by the user" should {
      "return 400" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "timeAtCurrentAddress" -> Seq("")
            ))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include(pageTitle)
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(errorMessage)
          }
        }
      }
    }
  }
}