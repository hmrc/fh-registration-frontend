package uk.gov.hmrc.fhregistrationfrontend.emailverification

import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class DraftApplication
  extends Specifications with TestConfiguration {

  "Loading a page w/o verified email" should {

    "redirect to " in {
      given
        .user.isAuthorised
        .save4later.hasBusinessInformationWOVerifiedEmail
        .audit.writesAuditOrMerged()

      WsTestClient withClient { implicit client ⇒
        val result = user.gets.contactPersonPage.futureValue

        result.status mustBe 303
        result.header("Location") mustBe Some("/fhdds/contact-email")
      }
    }
  }

}
