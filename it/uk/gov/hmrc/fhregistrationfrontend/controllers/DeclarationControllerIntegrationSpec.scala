package uk.gov.hmrc.fhregistrationfrontend.controllers

import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions.KeyStoreStub
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class DeclarationControllerIntegrationSpec
  extends Specifications with TestConfiguration {

  "DeclarationController" should {

    "Show the declaration page when the user has fulfilled all the pages" in {

      given
        .summaryPrecondition

      WsTestClient.withClient { client ⇒
        val result = client.url(s"$baseUrl/declaration").get()

        whenReady(result) { res ⇒
          res.status mustBe 200
        }

      }
    }

    "Post the declaration form" in {
      given
        .summaryPrecondition
        .fhddsBackend.createSubscription()

      WsTestClient.withClient { client ⇒
        val result =
          client.url(s"$baseUrl/submit").withFollowRedirects(false)
            .withHttpHeaders("X-Session-ID" → "some-id",
              "Csrf-Token" -> "nocheck",
              "Content-Type" → "application/json")
            .post(
              """{"fullName": "Tester",
                |"jobTitle": "Dev",
                |"usingDefaultEmail": "true",
                |"defaultEmail": "user@test.com"}""".stripMargin)


        whenReady(result) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/acknowledgement")
        }

      }

    }

  }

}
