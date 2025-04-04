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
import uk.gov.hmrc.fhregistrationfrontend.services.Encryption

case class SummaryConfirmationCache(
  id: String,
  fhSession: SummaryConfirmation
)

object SummaryConfirmationCache {
  object MongoFormats {
    implicit val cryptEncryptedValueFormats: Format[EncryptedValue] = CryptoFormats.encryptedValueFormat

    def reads()(implicit encryption: Encryption): Reads[SummaryConfirmationCache] =
      (
        (__ \ "id").read[String] and
          (__ \ "fhSession").read[EncryptedValue]
      )(ModelEncryption.decryptSessionCache _)

    def writes(implicit encryption: Encryption): OWrites[SummaryConfirmationCache] =
      new OWrites[SummaryConfirmationCache] {

        override def writes(sessionCache: SummaryConfirmationCache): JsObject = {
          val encryptedValue: (String, EncryptedValue) =
            ModelEncryption.encryptSessionCache(sessionCache)
          Json.obj(
            "id"        -> encryptedValue._1,
            "fhSession" -> encryptedValue._2
          )
        }
      }

    def formats(implicit encryption: Encryption): OFormat[SummaryConfirmationCache] = OFormat(reads(), writes)
  }
}
