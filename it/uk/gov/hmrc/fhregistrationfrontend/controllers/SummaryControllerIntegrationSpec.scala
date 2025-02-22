package uk.gov.hmrc.fhregistrationfrontend.controllers

import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions.{PreconditionBuilder, PreconditionHelpers}

class SummaryControllerIntegrationSpec
    extends Specifications with TestConfiguration with PreconditionHelpers {
  implicit val builder: PreconditionBuilder = new PreconditionBuilder()

  "SummaryController" should {

    "Show the summary page when the user has fulfilled all the pages" in {
      `given`.summaryPrecondition

      WsTestClient.withClient { client =>
        val result = client
          .url(s"$baseUrl/summary")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
        }

      }
    }

    "Show the bad request page when the user has not fulfilled all the pages" in {
      `given`.commonPrecondition

      WsTestClient.withClient { client =>
        val result = client
          .url(s"$baseUrl/summary")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 400
        }

      }
    }
  }

}
