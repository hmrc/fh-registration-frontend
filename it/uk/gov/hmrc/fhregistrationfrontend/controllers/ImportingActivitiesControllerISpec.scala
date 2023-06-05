package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class ImportingActivitiesControllerISpec
  extends Specifications with TestConfiguration {
  val requestUrl = "importingActivities"

  "GET /importingActivities" when {

    "render the importing activities page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("Does the business have an EORI (Economic Operator Registration and Identification) number?")
            page.getElementsByTag("h1").text must include("Does the business have an EORI (Economic Operator Registration and Identification) number?")
          }
        }
      }
    }

  }

  "POST /importingActivities" when {

    "the user selects no" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "hasEoriMapping" -> Seq("false")
              ))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Form submitted, with result: TradingName(true,Some(Shelby Company Limited))")
            }
          }
        }
      }
    }
  }
}