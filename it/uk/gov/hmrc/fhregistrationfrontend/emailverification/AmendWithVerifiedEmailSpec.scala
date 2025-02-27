package uk.gov.hmrc.fhregistrationfrontend.emailverification

import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.api.libs.ws.writeableOf_urlEncodedForm
import uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions.PreconditionBuilder

class AmendWithVerifiedEmailSpec extends Specifications with TestConfiguration {

  "Submitting an amendment" should {
    "Allow submission" when {
      "The verified email was amended" in {

        `given`.user.isAuthorisedAndEnrolled.save4later
          .acceptsDelete()
          .audit
          .writesAuditOrMerged()
          .fhddsBackend
          .acceptsAmendments()
          .save4later
          .hasAmendmentDataWithNewVerifiedEmail("a@test.com")

        WsTestClient.withClient { implicit client =>
          val result = client
            .url(s"$baseUrl/submit")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders("X-Session-ID" -> "some-id", "Csrf-Token" -> "nocheck")
            .withFollowRedirects(false)
            .post(
              Map(
                "fullName"          -> Seq("Tester"),
                "jobTitle"          -> Seq("Dev"),
                "usingDefaultEmail" -> Seq("true"),
                "defaultEmail"      -> Seq("a@test.com")
              )
            )

          whenReady(result) { res =>
            res.status mustBe 303
            res.header("Location") mustBe Some("/fhdds/acknowledgement")

            expect.fhddsBackend.amendWasCalled.fhddsBackend
              .contactEmailMatches("a@test.com")
              .fhddsBackend
              .contactDetailChangedFlag(true)

          }
        }
      }
    }
  }
}
