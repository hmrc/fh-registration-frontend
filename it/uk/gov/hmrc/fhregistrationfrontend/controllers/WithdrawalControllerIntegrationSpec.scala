package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.api.libs.ws.writeableOf_urlEncodedForm
import uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions.MockHelper

class WithdrawalControllerIntegrationSpec
    extends Specifications with TestConfiguration with MockitoSugar with MockHelper {

  "WithdrawalController" should {

    "Ask the reason for the withdrawal" in {

      setupWritesAuditOrMergedMocks()
      setupIsAuthorisedAndEnrolledMocks()

      WsTestClient.withClient { client =>
        val result1 = client
          .url(s"$baseUrl/withdraw")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()
        val result2 = client
          .url(s"$baseUrl/withdraw/reason")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/withdraw/reason")
        }
        whenReady(result2) { res =>
          res.status mustBe 200
        }
      }
    }

    "Post the reason for the withdrawal" in {

      setupWritesAuditOrMergedMocks()
      setupIsAuthorisedAndEnrolledMocks()
      setupSaveWithdrawalReasonMocks(sessionId)

      WsTestClient.withClient { client =>
        val result =
          client
            .url(s"$baseUrl/withdraw/reason")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("reason" -> Seq("Applied in Error")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/withdraw/confirm")
        }

      }
    }

    "Handle the reason and let the user to confirm withdraw" in {

      setupWritesAuditOrMergedMocks()
      setupIsAuthorisedAndEnrolledMocks()
      setupSaveWithdrawalReasonMocks(sessionId)
      setupGetSubscriptionMocks()

      WsTestClient.withClient { client =>
        val result =
          client
            .url(s"$baseUrl/withdraw/confirm")
            .withHttpHeaders(xSessionId)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

        whenReady(result) { res =>
          res.status mustBe 200
        }
      }
    }

    "Show bad request to the user if not enrolled" in {

      setupCommonPreconditionMocks()

      WsTestClient.withClient { client =>
        val result1 = client
          .url(s"$baseUrl/withdraw")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()
        val result2 = client
          .url(s"$baseUrl/withdraw/reason")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/withdraw/reason")
        }
        whenReady(result2) { res =>
          res.status mustBe 400
        }
      }
    }

  }

}
