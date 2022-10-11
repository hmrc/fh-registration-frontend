package uk.gov.hmrc.fhregistrationfrontend.emailverification

import org.scalatest.Ignore
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}


class AmendWithVerifiedEmailSpec
  extends Specifications with TestConfiguration {

  "Submitting an amendment" should {
    "Allow submission" when {
      "The verified email was amended" in {
        given
          .user.isAuthorisedAndEnrolled
          .save4later.acceptsDelete()
          .audit.writesAuditOrMerged()
          .fhddsBackend.acceptsAmendments()
          .save4later.hasAmendmentDataWithNewVerifiedEmail("a@test.com")

        // TODO: finish reworking so that alternative email is present - in above - previous submission has been done, now amending
        WsTestClient.withClient { implicit client ⇒
          val result = client.url(s"$baseUrl/declaration")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .post(Map("fullName" -> Seq("Tester"),
              "jobTitle" -> Seq("Dev"),
              "usingDefaultEmail" -> Seq("true"),
              "v" -> Seq("user@test.com")
            ))

          whenReady(result) { res ⇒
            res.status mustBe 303
            res.header("Location") mustBe Some("/fhdds/acknowledgement")


            expect
              .fhddsBackend.amendWasCalled
              .fhddsBackend.contactEmailMatches("a@test.com")
              .fhddsBackend.contactDetailChangedFlag(true)
          }
        }
      }

    }

  }
}