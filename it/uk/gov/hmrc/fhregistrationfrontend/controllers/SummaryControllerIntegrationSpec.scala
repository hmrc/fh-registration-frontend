package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions.{MockHelper, PreconditionBuilder, PreconditionHelpers}

class SummaryControllerIntegrationSpec
    extends Specifications with TestConfiguration with PreconditionHelpers with MockitoSugar with MockHelper {
  implicit val builder: PreconditionBuilder = new PreconditionBuilder()

  "SummaryController" should {

    "Show the summary page when the user has fulfilled all the pages" in {
      setupSummaryPreconditionMocks()

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
      setupCommonPreconditionMocks()

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
