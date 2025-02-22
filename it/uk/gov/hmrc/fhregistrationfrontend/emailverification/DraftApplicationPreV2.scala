package uk.gov.hmrc.fhregistrationfrontend.emailverification

import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions.MockHelper
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class DraftApplicationPreV2 extends Specifications with TestConfiguration with MockitoSugar with MockHelper {

  "Loading a draft application pre2.0" should {
    "redirect the user to email-verification-status" when {
      "the user loads the summary page" in {

        setupUserIsAuthorisedMocks()
        setupHasFullPreEmailVerificationDataMocks()
        setupWritesAuditOrMergedMocks()

        WsTestClient withClient { implicit client =>
          val result = client
            .url(s"$baseUrl/summary")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header("Location") mustBe Some("/fhdds/email-verification-status")
          }
        }
      }

      "the user loads a form page" in {

        setupUserIsAuthorisedMocks()
        setupHasFullPreEmailVerificationDataMocks()
        setupWritesAuditOrMergedMocks()

        WsTestClient withClient { implicit client =>
          val result = client
            .url(s"$baseUrl/form/mainBusinessAddressPage")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withFollowRedirects(false)
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header("Location") mustBe Some("/fhdds/email-verification-status")
          }
        }
      }

      "the user loads the declaration page" in {

        setupUserIsAuthorisedMocks()
        setupHasFullPreEmailVerificationDataMocks()
        setupWritesAuditOrMergedMocks()

        WsTestClient withClient { implicit client =>
          val result = client
            .url(s"$baseUrl/declaration")
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
}
