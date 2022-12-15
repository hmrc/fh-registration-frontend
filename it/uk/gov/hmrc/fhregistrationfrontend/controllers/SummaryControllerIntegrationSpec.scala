package uk.gov.hmrc.fhregistrationfrontend.controllers

import play.api.http.HeaderNames
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class SummaryControllerIntegrationSpec
  extends Specifications with TestConfiguration {

  "SummaryController" should {

    "Show the summary page when the user has fulfilled all the pages" in {

      given
        .summaryPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/summary")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
        }

      }
    }

    "Show the bad request page when the user has not fulfilled all the pages" in {
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/summary")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 400
        }

      }
    }
  }

}
