package uk.gov.hmrc.fhddsfrontend.controllers

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.WsScalaTestClient
import play.api.http.HeaderNames
import play.api.test.WsTestClient
import uk.gov.hmrc.fhddsfrontend.testsupport.TestedApplication

import scala.concurrent.Await
import scala.concurrent.duration._

class ApplicationControllerIntegrationSpec
  extends WordSpec
    with OptionValues
    with WsScalaTestClient
    with TestedApplication
    with WordSpecLike
    with Matchers
    with ScalaFutures {


  "Application" should {
    "be reachable" in {
      when()
        .audit.writesAuditOrMerged()
      WsTestClient.withClient { client ⇒
        Await.result(client.url(s"http://localhost:$port/ping/ping").get(), 10 seconds).status shouldBe (200)
      }
    }

    "not authorize w/o the whitelisting token" in {
      when()
        .audit.writesAuditOrMerged()
      WsTestClient.withClient { client ⇒
        whenReady(client.url(s"http://localhost:$port/fhdds").get()) { res ⇒
          res.status shouldBe 401
        }
      }
    }


    "redirect to the login page if the user is not logged in" in {
      when()
        .audit.writesAuditOrMerged()
        .user.isNotAuthorised()

      WsTestClient.withClient { client ⇒
        whenReady(client.url(s"http://localhost:$port/fhdds/whitelisted?p=123124").withFollowRedirects(false).get()) { res ⇒
          res.status shouldBe 303
          res.header(HeaderNames.LOCATION).get shouldBe "http://localhost:9025/gg/sign-in?continue=http%3A%2F%2Flocalhost%3A1118%2Fwhitelisted%3Fp%3D123124&origin=fhdds-frontend"
        }
      }
    }

    "redirect to the verification FE if logged in" in {
      when()
        .audit.writesAuditOrMerged()
        .user.isAuthorised()

      WsTestClient.withClient { client ⇒
        whenReady(client.url(s"http://localhost:$port/fhdds/whitelisted?p=123124").withFollowRedirects(false).get()) { res ⇒
          res.status shouldBe 303
          res.header(HeaderNames.LOCATION).get shouldBe "http://localhost:9227/verification/otac/login?p=123124"
        }
      }
    }
  }

}
