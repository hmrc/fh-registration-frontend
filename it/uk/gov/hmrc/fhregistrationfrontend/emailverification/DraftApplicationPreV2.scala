package uk.gov.hmrc.fhregistrationfrontend.emailverification

import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class DraftApplicationPreV2
  extends Specifications with TestConfiguration {


  "Loading a draft application pre2.0" should {
    "redirect the user to email-verification-status" when {
      "the user loads the summary page" in {
        given
          .user.isAuthorised
          .save4later.hasFullPreEmailVerificationData()
          .audit.writesAuditOrMerged()

        WsTestClient withClient { implicit client ⇒
          val result = user.gets.summaryPage.futureValue
          println(result.body)
          result.status mustBe 303
          result.header("Location") mustBe Some("/fhdds/email-verification-status")

        }
      }

      "the user loads a form page" in {
        given
          .user.isAuthorised
          .save4later.hasFullPreEmailVerificationData()
          .audit.writesAuditOrMerged()

        WsTestClient withClient { implicit client ⇒
          val result = user.gets.mainBusinessAddressPage.futureValue
          result.status mustBe 303
          result.header("Location") mustBe Some("/fhdds/email-verification-status")
        }
      }

      "the user loads the declaration page" in {
        given
          .user.isAuthorised
          .save4later.hasFullPreEmailVerificationData()
          .audit.writesAuditOrMerged()

        WsTestClient withClient { implicit client ⇒
          val result = user.gets.declarationPage.futureValue
          result.status mustBe 303
          result.header("Location") mustBe Some("/fhdds/email-verification-status")
        }
      }

    }
  }



}
