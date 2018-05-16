package uk.gov.hmrc.fhregistrationfrontend.emailverification

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

        WsTestClient.withClient { implicit client ⇒
          val result = user.calls.declarationSubmit.futureValue
          result.status mustBe 303
          result.header("Location") mustBe Some("/fhdds/acknowledgement")

          expect
            .fhddsBackend.amendWasCalled
            .fhddsBackend.contactEmailMatches("a@test.com")
            .fhddsBackend.contactDetailChangedFlag(true)
        }
      }
    }

  }

}
