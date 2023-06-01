package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessCustomersControllerISpec
  extends Specifications with TestConfiguration {

  "GET /businessCustomers" when {

    "render the business customers page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/businessCustomers")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("How many overseas customers established outside the UK does the business store goods for, currently?")
            page.getElementsByTag("h1").text must include("How many overseas customers established outside the UK does the business store goods for, currently?")
            page.getElementById("numberOfCustomers-hint").text must include("You only need to tell us about the part of the business that deals with fulfilment.")
          }
        }
      }
    }

  }

  "POST /businessCustomers" when {

    "the user selects" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/businessCustomers")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "numberOfCustomers" -> Seq("11-50")
              ))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Next page! with form result: BusinessCustomers(11-50)")
            }
          }
        }
      }

      "return 400" when {
        "the user doesn't select a radio button" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/businessCustomers")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "numberOfCustomers" -> Seq("")
              ))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("How many overseas customers established outside the UK does the business store goods for, currently?")
              page.getElementsByClass("govuk-error-message").text() must include("Select how many sellers established outside the UK the business currently stores goods for")
            }
          }
        }
      }

    }
  }
}