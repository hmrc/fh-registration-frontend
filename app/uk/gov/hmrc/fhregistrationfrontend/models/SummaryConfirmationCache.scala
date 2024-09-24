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
import java.time.{Instant, LocalDateTime}

case class SummaryConfirmationCache(
  id: String,
  fhSession: SummaryConfirmation,
  createdAt: LocalDateTime,
  lastModified: Instant = Instant.now()
)

object SummaryConfirmationCache {
  object MongoFormats {
    import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._
    implicit val cryptEncryptedValueFormats: Format[EncryptedValue] = CryptoFormats.encryptedValueFormat

    def reads()(implicit encryption: Encryption): Reads[SummaryConfirmationCache] =
      (
        (__ \ "id").read[String] and
          (__ \ "fhSession").read[EncryptedValue] and
          (__ \ "createdAt").read[LocalDateTime] and
          (__ \ "lastModified").read[Instant]
      )(ModelEncryption.decryptSessionCache _)

    def writes(implicit encryption: Encryption): OWrites[SummaryConfirmationCache] =
      new OWrites[SummaryConfirmationCache] {

        override def writes(sessionCache: SummaryConfirmationCache): JsObject = {
          val encryptedValue: (String, EncryptedValue, LocalDateTime, Instant) =
            ModelEncryption.encryptSessionCache(sessionCache)
          Json.obj(
            "id"           -> encryptedValue._1,
            "fhSession"    -> encryptedValue._2,
            "createdAt"    -> encryptedValue._3,
            "lastModified" -> encryptedValue._4
          )
        }
      }

    def formats(implicit encryption: Encryption): OFormat[SummaryConfirmationCache] = OFormat(reads(), writes)
  }
}
