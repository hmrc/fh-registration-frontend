package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.scalatestplus.mockito.MockitoSugar
import play.api.http.HeaderNames
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions.MockHelper
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class FormPageControllerIntegrationSpec
    extends Specifications with TestConfiguration with MockitoSugar with MockHelper {

  "FormPageController" should {
    "Show the form's first page when the user has selected a business type and the user is new" in {

      setupBusinessTypeWasSavedMocks()

      WsTestClient.withClient { client =>
        val result1 = client
          .url(s"$baseUrl/resume")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withFollowRedirects(false)
          .get()

        val result2 = client
          .url(s"$baseUrl/startForm")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withFollowRedirects(false)
          .get()

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/form/contactPerson")
        }
        whenReady(result2) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/resume")
        }
      }
    }

    "Show the form's second page when the user has fulfilled the first page" in {

      setupSavePageDataMocks()

      WsTestClient.withClient { client =>
        val result = client
          .url(s"$baseUrl/form/contactPerson")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
        }
      }
    }

    "Show page not found when the user try to call the second or the other pages without fulfilled the first page" in {

      setupBusinessTypeWasSavedMocks()

      WsTestClient.withClient { client =>
        val result1 = client
          .url(s"$baseUrl/form/mainBusinessAddress")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withFollowRedirects(false)
          .get()
        whenReady(result1) { res =>
          res.status mustBe 404
        }

        val result2 = client
          .url(s"$baseUrl/form/companyRegistrationNumber")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withFollowRedirects(false)
          .get()
        whenReady(result2) { res =>
          res.status mustBe 404
        }
      }
    }

    "Load Importing Activities data correctly from save4Later when the it contains the split fields" in {
      setupImportingActivitiesMocks()

      WsTestClient.withClient { client =>
        val result = client
          .url(s"$baseUrl/summary")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
        }

      }
    }

    "Convert Importing Activities data correctly from save4Later when the it contains the EORI number model" in {
      setupImportingActivitiesMocks()

      WsTestClient.withClient { client =>
        val result = client
          .url(s"$baseUrl/summary")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
        }

      }
    }
  }

}
