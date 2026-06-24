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

import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.api.libs.ws.writeableOf_urlEncodedForm
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.{WithdrawalReason, WithdrawalReasonEnum}
import uk.gov.hmrc.fhregistrationfrontend.models.SummaryConfirmation
import scala.concurrent.duration.DurationInt
import scala.concurrent.Await

class WithdrawalControllerIntegrationSpec extends Specifications with TestConfiguration {

  val withdrawalReasonTestData: WithdrawalReason =
    WithdrawalReason(WithdrawalReasonEnum.NoLongerApplicable, Some("testData"))

  "WithdrawalController" should {

    "Ask the reason for the withdrawal" in {

      `given`.withdrawalPrecondition

      WsTestClient.withClient { client =>
        val result1 = client
          .url(s"$baseUrl/withdraw")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()
        val result2 = client
          .url(s"$baseUrl/withdraw/reason")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/withdraw/reason")
        }
        whenReady(result2) { res =>
          res.status mustBe 200
        }
      }
    }

    "Post the reason for the withdrawal" in {

      `given`.withdrawalPrecondition

      WsTestClient.withClient { client =>
        val result =
          client
            .url(s"$baseUrl/withdraw/reason")
            .withFollowRedirects(false)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("reason" -> Seq("Applied in Error")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/withdraw/confirm")
        }

      }
    }

    "Handle the reason and let the user to confirm withdraw" in {

      `given`.withdrawalPrecondition.fhddsBackend.getSubscription()

      Await.result(
        summaryConfirmationRepo.set(
          SummaryConfirmation(
            sessionId,
            None,
            Some(withdrawalReasonTestData),
            None
          )
        ),
        3.seconds
      )

      WsTestClient.withClient { client =>
        val result =
          client
            .url(s"$baseUrl/withdraw/confirm")
            .withHttpHeaders(xSessionId)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

        whenReady(result) { res =>
          res.status mustBe 200
        }
      }
    }

    "Show bad request to the user if not enrolled" in {

      `given`.commonPrecondition

      WsTestClient.withClient { client =>
        val result1 = client
          .url(s"$baseUrl/withdraw")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()
        val result2 = client
          .url(s"$baseUrl/withdraw/reason")
          .withFollowRedirects(false)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result1) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(s"/fhdds/withdraw/reason")
        }
        whenReady(result2) { res =>
          res.status mustBe 400
        }
      }
    }

  }

}
