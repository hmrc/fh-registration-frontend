package uk.gov.hmrc.fhregistrationfrontend.controllers

import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{TestConfiguration, TestHelper}

class WithdrawalControllerIntegrationSpec
  extends TestHelper with TestConfiguration {

  "WithdrawalController" should {

    "Ask the reason for the withdrawal" in {

      withdrawalPrecondition

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/withdraw").withFollowRedirects(false).get()
        val result2 = client.url(s"$baseUrl/withdraw/reason").withFollowRedirects(false).get()

        whenReady(result1) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/withdraw/reason")
        }
        whenReady(result2) { res ⇒
          res.status mustBe 200
        }
      }
    }

    "Post the reason for the withdrawal" in {

      withdrawalPrecondition
        .keyStore.saveWithdrawalReason()

      WsTestClient.withClient { client ⇒
        val result =
          client.url(s"$baseUrl/withdraw/reason").withFollowRedirects(false)
            .withHeaders("X-Session-ID" → "some-id",
              "Csrf-Token" -> "nocheck",
              "Content-Type" → "application/json")
            .post("""{"reason": "Applied in Error"}""")

        whenReady(result) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/withdraw/confirm")
        }

      }
    }

    "Handle the reason and let the user to confirm withdraw" in {

      withdrawalPrecondition
        .keyStore.fetchWithdrawalReason()

      WsTestClient.withClient { client ⇒
        val result =
          client.url(s"$baseUrl/withdraw/confirm").withHeaders("X-Session-ID" → "some-id").get()

        whenReady(result) { res ⇒
          res.status mustBe 200
        }
      }
    }

    "Show bad request to the user if not enrolled" in {

      commonPrecondition

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/withdraw").withFollowRedirects(false).get()
        val result2 = client.url(s"$baseUrl/withdraw/reason").withFollowRedirects(false).get()

        whenReady(result1) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/withdraw/reason")
        }
        whenReady(result2) { res ⇒
          res.status mustBe 400
        }
      }
    }

  }

}
