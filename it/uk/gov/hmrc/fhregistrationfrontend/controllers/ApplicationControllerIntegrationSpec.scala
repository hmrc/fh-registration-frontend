/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.fhregistrationfrontend.controllers

import play.api.http.HeaderNames
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.ws.{DefaultWSCookie, WSResponse}

class ApplicationControllerIntegrationSpec extends Specifications with TestConfiguration with MockitoSugar {

  "Application" should {

    "be reachable" in {
      `given`.audit.writesAuditOrMerged()

      WsTestClient.withClient { client =>
        whenReady(
          client
            .url(s"http://localhost:$port/ping/ping")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()
        ) { result =>
          result.status mustBe 200
        }
      }
    }

    "redirects to the login page if the user is not logged in" in {
      `given`.audit.writesAuditOrMerged().user.isNotAuthorised()

      WsTestClient.withClient { client =>
        whenReady(
          client
            .url(s"$baseUrl")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()
        ) { res =>
          res.status mustBe 303
          res
            .header(HeaderNames.LOCATION)
            .get mustBe "http://localhost:9553/bas-gateway/sign-in?continue_url=http%3A%2F%2Flocalhost%3A1118%2Ffhdds&origin=fh-registration-frontend"
        }
      }
    }

    "redirect to the verification of main business address if logged in" in {
      `given`.audit.writesAuditOrMerged().user.isAuthorised().fhddsBackend.hasNoEnrolmentProgress()

      WsTestClient.withClient { client =>
        whenReady(
          client
            .url(s"$baseUrl")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()
        ) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION).get mustBe "/fhdds/start"
        }
      }

      WsTestClient.withClient { client =>
        whenReady(
          client
            .url(s"$baseUrl/start")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()
        ) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION).get mustBe "http://localhost:9923/business-customer/FHDDS"
        }
      }
    }

    "continue redirects to business type page when the user has a correct BPR and the user is new" in {

      `given`.commonPrecondition

      WsTestClient.withClient { client =>
        val result = client
          .url(s"$baseUrl/continue")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/businessType")
        }
      }
    }

    "continue redirects to forbidden page when the user is assistant" in {

      `given`.commonPreconditionAssist

      WsTestClient.withClient { client =>
        val result = client
          .url(s"$baseUrl/continue")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 403
          res.body.toString.contains("You cannot access this page") mustBe true
        }
      }
    }

    "continue redirects to bad request page when the user is not admin/user or assistant" in {

      `given`.commonPreconditionNoRole

      WsTestClient.withClient { client =>
        val result = client
          .url(s"$baseUrl/continue")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 400
          res.body.toString.contains("Bad Request") mustBe true
        }
      }
    }
  }
}
