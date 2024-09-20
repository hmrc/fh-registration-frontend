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
import uk.gov.hmrc.fhregistrationfrontend.controllers.ControllerSpecWithGuiceApp
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.{DeregistrationReason, DeregistrationReasonEnum}
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.{WithdrawalReason, WithdrawalReasonEnum}

class SummaryConfirmationSpec extends ControllerSpecWithGuiceApp {

  val id: String = "1"
  val summaryForPrintKey: String = "testData"
  val withdrawalReason: WithdrawalReason = WithdrawalReason(WithdrawalReasonEnum.NoLongerApplicable, Some("testData"))
  val deregistrationReason: DeregistrationReason =
    DeregistrationReason(DeregistrationReasonEnum.NoLongerNeeded, Some("testData"))

  val summaryConfirmation: SummaryConfirmation = SummaryConfirmation(
    id = "id",
    summaryForPrintKey = Some(summaryForPrintKey),
    withdrawalReason = Some(withdrawalReason),
    deregistrationReason = Some(deregistrationReason)
  )

  "SummaryConfirmation Encryption" should {
    "Encrypt data" in {
      val result = ModelEncryption.encryptSessionCache(summaryConfirmation)

      result._1 mustBe summaryConfirmation.id
      result._2 mustBe summaryConfirmation.summaryForPrintKey
      result._3 mustBe summaryConfirmation.withdrawalReason
      result._4 mustBe summaryConfirmation.deregistrationReason
    }
    "Decrypt data into model" in {
      val result = ModelEncryption.decryptSessionCache(
        id = summaryConfirmation.id,
        summaryForPrintKey = summaryConfirmation.summaryForPrintKey,
        withdrawalReason = summaryConfirmation.withdrawalReason,
        deregistrationReason = summaryConfirmation.deregistrationReason
      )
      result mustBe summaryConfirmation
    }
  }

}
