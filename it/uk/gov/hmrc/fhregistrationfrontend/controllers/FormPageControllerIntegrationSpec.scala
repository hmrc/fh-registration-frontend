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
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{EoriNumber, ImportingActivities}
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class FormPageControllerIntegrationSpec extends Specifications with TestConfiguration {

  "FormPageController" should {
    "Show the form's first page when the user has selected a business type and the user is new" in {

      `given`.commonPrecondition.save4later.businessTypeWasSaved()

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

      `given`.commonPrecondition.save4later
        .businessTypeWasSaved()
        .save4later
        .savePageData("mainBusinessAddress", """{"timeAtCurrentAddress": "3-5 years"}""")

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

      `given`.commonPrecondition.save4later.businessTypeWasSaved()

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
      val importingActivitiesWithSplitFields =
        ImportingActivities(hasEori = true, eori = Some("1234123132"), goodsImported = Some(true))

      `given`.commonPrecondition.save4later.hasFullFormDataWithImportingActivities(importingActivitiesWithSplitFields)

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
      val importingActivitiesWithEoriNumberModel = ImportingActivities(
        hasEori = true,
        eoriNumber = Some(EoriNumber(eoriNumber = "1234123132", goodsImportedOutsideEori = true))
      )

      `given`.commonPrecondition.save4later.hasFullFormDataWithImportingActivities(
        importingActivitiesWithEoriNumberModel
      )

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
