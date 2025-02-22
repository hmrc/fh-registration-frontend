package uk.gov.hmrc.fhregistrationfrontend.emailverification

import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions.{MockHelper, PreconditionBuilder}
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class DraftApplication extends Specifications with TestConfiguration with MockitoSugar with MockHelper {

  "Loading a page w/o verified email" should {

    "redirect to email verification page" in {

      setupUserIsAuthorisedMocks()
      setupHasBusinessInformationWOVerifiedEmailMocks()
      setupWritesAuditOrMergedMocks()

      WsTestClient withClient { implicit client =>
        val result = client
          .url(s"$baseUrl/form/contactPerson")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withFollowRedirects(false)
          .get()

        whenReady(result) { res =>
          res.status mustBe 303
          res.header("Location") mustBe Some("/fhdds/email-verification-status")
        }
      }
    }
  }
}
