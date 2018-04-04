package uk.gov.hmrc.fhregistrationfrontend.controllers

import play.api.http.HeaderNames
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{TestConfiguration, TestHelper}

class ApplicationControllerIntegrationSpec
  extends TestHelper with TestConfiguration {

  "Application" should {
    val baseUrl = s"http://localhost:$port/fhdds"
    "be reachable" in {
      given()
        .audit.writesAuditOrMerged()

      WsTestClient.withClient { client ⇒
        whenReady(client.url(s"http://localhost:$port/ping/ping").get()) {result ⇒
          result.status mustBe 200
        }
      }
    }

    "redirects to the login page if the user is not logged in" in {
      given()
        .audit.writesAuditOrMerged()
        .user.isNotAuthorised()

      WsTestClient.withClient { client ⇒
        whenReady(client.url(s"$baseUrl").withFollowRedirects(false).get()) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION).get mustBe "http://localhost:9025/gg/sign-in?continue=http%3A%2F%2Flocalhost%3A1118&origin=fh-registration-frontend"
        }
      }
    }

    "redirects to the verification of main business address if logged in" in {
      given()
        .audit.writesAuditOrMerged()
        .user.isAuthorised()

      WsTestClient.withClient { client ⇒
        whenReady(client.url(s"$baseUrl").withFollowRedirects(false).get()) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION).get mustBe "http://localhost:9923/business-customer/FHDDS?backLinkUrl=http://localhost:1118/fhdds/continue"
        }
      }
    }

    "continue redirect to form type page when the user has a correct BPR and the user is new" in {

      commonPrecondition

      WsTestClient.withClient { client ⇒
        val result = client.url(s"$baseUrl/continue").withFollowRedirects(false).get()

        whenReady(result) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/deleteOrContinue?isNewForm=true")
        }
      }
    }

    "Show form type page when the user has a correct BPR and the user is new" in {

      commonPrecondition

      WsTestClient.withClient { client ⇒
        val result = client.url(s"$baseUrl/deleteOrContinue?isNewForm=true").withFollowRedirects(false).get()

        whenReady(result) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/businessType")
        }
      }
    }

    "Show the form's first page when the user has selected a business type and the user is new" in {

      commonPrecondition
        .save4later.businessTypeHasSaved()

      WsTestClient.withClient { client ⇒
        val result1 = client.url(s"$baseUrl/resume")
          .withFollowRedirects(false)
          .get()

        val result2 = client.url(s"$baseUrl/startForm")
          .withFollowRedirects(false)
          .get()

        whenReady(result1) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/startForm")
        }
        whenReady(result2) { res ⇒
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/form/mainBusinessAddress")
        }
      }
    }

  }

}
