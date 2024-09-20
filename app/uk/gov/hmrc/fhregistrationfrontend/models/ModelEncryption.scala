/*
 * Copyright 2023 HM Revenue & Customs
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

import models.UserAnswers
import play.api.libs.json._
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.DeregistrationReason
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.WithdrawalReason
import uk.gov.hmrc.fhregistrationfrontend.services.Encryption

import java.time.Instant

object ModelEncryption {
  def encryptUserAnswers(
    userAnswers: UserAnswers
  )(implicit encryption: Encryption): (String, Map[String, EncryptedValue], Instant) = {
    val encryptedDataValues = userAnswers.data
      .map { case (key, value) =>
        key -> encryption.crypto.encrypt(value.toString(), userAnswers.id)
      }
    (userAnswers.id, encryptedDataValues, userAnswers.lastUpdated)
  }

  def decryptUserAnswers(id: String, data: Map[String, EncryptedValue], lastUpdated: Instant)(implicit
    encryption: Encryption
  ): UserAnswers = {
    val decryptedUAData = data
      .map { case (key, encrytedValue) =>
        key -> Json.parse(encryption.crypto.decrypt(encrytedValue, id))
      }
    UserAnswers(
      id = id,
      data = decryptedUAData,
      lastUpdated = lastUpdated
    )
  }

  def encryptSessionCache(
    sessionCache: SummaryConfirmation
  ): (String, Option[String], Option[WithdrawalReason], Option[DeregistrationReason]) =
    (
      sessionCache.id,
      sessionCache.summaryForPrintKey,
      sessionCache.withdrawalReason,
      sessionCache.deregistrationReason
    )

  def decryptSessionCache(
    id: String,
    summaryForPrintKey: Option[String],
    withdrawalReason: Option[WithdrawalReason],
    deregistrationReason: Option[DeregistrationReason]
  ): SummaryConfirmation =
    SummaryConfirmation(
      id = id,
      summaryForPrintKey = summaryForPrintKey,
      withdrawalReason = withdrawalReason,
      deregistrationReason = deregistrationReason
    )
}
