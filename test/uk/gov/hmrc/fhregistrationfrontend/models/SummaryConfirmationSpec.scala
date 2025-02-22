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

import org.scalatest.matchers.must.Matchers.mustBe
import play.api.libs.json.Json
import uk.gov.hmrc.fhregistrationfrontend.controllers.ControllerSpecWithGuiceApp
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.{DeregistrationReason, DeregistrationReasonEnum}
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.{WithdrawalReason, WithdrawalReasonEnum}
import uk.gov.hmrc.fhregistrationfrontend.services.Encryption

class SummaryConfirmationSpec extends ControllerSpecWithGuiceApp {
  implicit val encryption: Encryption = app.injector.instanceOf[Encryption]

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

  val fhSessionCache: SummaryConfirmationCache = SummaryConfirmationCache(
    id = "id",
    fhSession = summaryConfirmation
  )

  "SummaryConfirmation Encryption" should {
    "Encrypt data" in {
      val result = ModelEncryption.encryptSessionCache(fhSessionCache)

      result._1 mustBe fhSessionCache.id
      Json
        .parse(encryption.crypto.decrypt(result._2, fhSessionCache.id))
        .as[SummaryConfirmation] mustBe fhSessionCache.fhSession
    }

    "Decrypt data into model" in {
      val result = ModelEncryption.decryptSessionCache(
        id = fhSessionCache.id,
        fhSession = encryption.crypto.encrypt(Json.toJson(fhSessionCache.fhSession).toString, fhSessionCache.id)
      )
      result mustBe fhSessionCache
    }
  }

}
