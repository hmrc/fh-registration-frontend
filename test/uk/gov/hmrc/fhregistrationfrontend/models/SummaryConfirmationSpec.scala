/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.models

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.fhregistrationfrontend.controllers.ControllerSpecWithGuiceApp
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.{DeregistrationReason, DeregistrationReasonEnum}
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.{WithdrawalReason, WithdrawalReasonEnum}

class SummaryConfirmationSpec extends ControllerSpecWithGuiceApp {

  val id: String = "1"
  val summaryForPrintKey: String = "testData"
  val withdrawalReason: WithdrawalReason = WithdrawalReason(WithdrawalReasonEnum.NoLongerApplicable, Some("testData"))
  val deregistrationReason: DeregistrationReason =
    DeregistrationReason(DeregistrationReasonEnum.NoLongerNeeded, Some("testData"))
  val summaryConfirmation: SummaryConfirmation =
    new SummaryConfirmation(id, Some(summaryForPrintKey), Some(withdrawalReason), Some(deregistrationReason))
  val summaryConfirmationJson: JsValue = Json.parse(
    """{
      | "id":"1",
      | "summaryForPrintKey":"testData",
      | "withdrawalReason":
      |   {"withdrawalReason":"No Longer Applicable",
      |    "withdrawalReasonOther":"testData"},
      | "deregistrationReason":
      |   {"deregistrationReason":"CEASES_REGISTERABLE_SCHEME",
      |    "deregistrationReasonOther":"testData"}
      |}
      |""".stripMargin
  )

  "SummaryConfirmation" should {
    "convert model to json" in {
      Json.toJson(summaryConfirmation) mustBe summaryConfirmationJson
    }
    "convert Json to model" in {
      Json.fromJson[SummaryConfirmation](summaryConfirmationJson).get mustBe summaryConfirmation
    }
  }
}
