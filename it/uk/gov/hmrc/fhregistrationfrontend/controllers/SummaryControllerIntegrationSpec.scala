package uk.gov.hmrc.fhregistrationfrontend.controllers

import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{TestConfiguration, TestHelper}

class SummaryControllerIntegrationSpec
  extends TestHelper with TestConfiguration {

  "SummaryController" should {

    "Show the summary page when the user has fulfilled all the pages" in {

      summaryPrecondition

      WsTestClient.withClient { client ⇒
        val result = client.url(s"$baseUrl/summary").get()

        whenReady(result) { res ⇒
          res.status mustBe 200
        }

      }
    }

    "Show the bad request page when the user has not fulfilled all the pages" in {

      commonPrecondition

      WsTestClient.withClient { client ⇒
        val result = client.url(s"$baseUrl/summary").get()

        whenReady(result) { res ⇒
          res.status mustBe 400
        }

      }
    }
  }

}
