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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.crypto.json.CryptoFormats
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.DeregistrationReason
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.WithdrawalReason

case class SummaryConfirmation(
  id: String,
  summaryForPrintKey: Option[String],
  withdrawalReason: Option[WithdrawalReason],
  deregistrationReason: Option[DeregistrationReason]
)

object SummaryConfirmation {
  object MongoFormats {
    implicit val cryptEncryptedValueFormats: Format[EncryptedValue] = CryptoFormats.encryptedValueFormat

    def reads(): Reads[SummaryConfirmation] =
      (
        (__ \ "id").read[String] and
          (__ \ "summaryForPrintKey").readNullable[String] and
          (__ \ "withdrawalReason").readNullable[WithdrawalReason] and
          (__ \ "deregistrationReason").readNullable[DeregistrationReason]
      )(ModelEncryption.decryptSessionCache _)

    def writes: OWrites[SummaryConfirmation] = new OWrites[SummaryConfirmation] {

      override def writes(sessionCache: SummaryConfirmation): JsObject = {
        val encryptedValue: (String, Option[String], Option[WithdrawalReason], Option[DeregistrationReason]) =
          ModelEncryption.encryptSessionCache(sessionCache)
        Json.obj(
          "id"                   -> encryptedValue._1,
          "summaryForPrintKey"   -> encryptedValue._2,
          "withdrawalReason"     -> encryptedValue._3,
          "deregistrationReason" -> encryptedValue._4
        )
      }
    }

    def formats: OFormat[SummaryConfirmation] = OFormat(reads(), writes)
  }
}
