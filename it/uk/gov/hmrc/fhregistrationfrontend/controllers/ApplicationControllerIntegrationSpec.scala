package uk.gov.hmrc.fhregistrationfrontend.controllers

import play.api.http.HeaderNames
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class ApplicationControllerIntegrationSpec
  extends Specifications with TestConfiguration {

  "Application" should {

    "be reachable" in {
      given
        .audit.writesAuditOrMerged()

      WsTestClient.withClient { client ⇒
        whenReady(client.url(s"http://localhost:$port/ping/ping").get()) {result ⇒
          result.status mustBe 200
        }
      }
    }

    "redirects to the login page if the user is not logged in" in {
      given
        .audit.writesAuditOrMerged()
        .user.isNotAuthorised()

      WsTestClient.withClient { client ⇒
        whenReady(client.url(s"$baseUrl").withFollowRedirects(false).get()) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION).get mustBe "http://localhost:9025/gg/sign-in?continue=http%3A%2F%2Flocalhost%3A1118&origin=fh-registration-frontend"
        }
      }
    }

    "redirect to the verification of main business address if logged in" in {
      given
        .audit.writesAuditOrMerged()
        .user.isAuthorised()
        .fhddsBackend.hasNoEnrolmentProgress()

      WsTestClient.withClient { client ⇒
        whenReady(client.url(s"$baseUrl").withFollowRedirects(false).get()) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION).get mustBe "/fhdds/start"
        }
      }

      WsTestClient.withClient { client ⇒
        whenReady(client.url(s"$baseUrl/start").withFollowRedirects(false).get()) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION).get mustBe "http://localhost:9923/business-customer/FHDDS"
        }
      }
    }

    "continue redirects to business type page when the user has a correct BPR and the user is new" in {

      given
          .commonPrecondition

      WsTestClient.withClient { client ⇒
        val result = client.url(s"$baseUrl/continue").withFollowRedirects(false).get()

        whenReady(result) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/businessType")
        }
      }
    }

  }

}
