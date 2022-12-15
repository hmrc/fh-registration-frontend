package uk.gov.hmrc.fhregistrationfrontend.controllers

import play.api.libs.ws.DefaultWSCookie
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

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/declaration")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
        }

      }
    }

    "Post the declaration form" in {
      given
        .summaryPrecondition
        .fhddsBackend.createSubscription()

      WsTestClient.withClient { client =>
        val result =
          client.url(s"$baseUrl/submit")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))

            .withHttpHeaders("X-Session-ID" → "some-id",
              "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(Map("fullName" -> Seq("Tester"),
              "jobTitle" -> Seq("Dev"),
              "usingDefaultEmail" -> Seq("true"),
              "defaultEmail" -> Seq("user@test.com")
            ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/acknowledgement")
        }

      }

    }

  }

}
