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

package models

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.crypto.json.CryptoFormats
import uk.gov.hmrc.fhregistrationfrontend.models.ModelEncryption
import uk.gov.hmrc.fhregistrationfrontend.services.Encryption

import java.time.Instant

final case class UserAnswers(
  id: String,
  data: Map[String, JsValue] = Map.empty[String, JsValue],
  lastUpdated: Instant = Instant.now
) {

  def getDataEntry[A](key: String)(implicit rds: Reads[A]): Option[A] =
    data
      .get(key)
      .map(_.as[A])

  def setDataEntry[A](key: String, value: A)(implicit writes: Writes[A]): UserAnswers = {
    val updatedData = data + (key -> Json.toJson(value))
    copy(data = updatedData)
  }

  def removeDataEntry(key: String): UserAnswers = {
    val updatedData = data.removed(key)
    copy(data = updatedData)
  }
}

object UserAnswers {
  object MongoFormats {
    implicit val cryptEncryptedValueFormats: Format[EncryptedValue] = CryptoFormats.encryptedValueFormat

    import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._
    def reads(implicit encryption: Encryption): Reads[UserAnswers] =
      (
        (__ \ "_id").read[String] and
          (__ \ "data").read[Map[String, EncryptedValue]] and
          (__ \ "lastUpdated").read[Instant]
      )(ModelEncryption.decryptUserAnswers _)

    def writes(implicit encryption: Encryption): OWrites[UserAnswers] = new OWrites[UserAnswers] {
      override def writes(userAnswers: UserAnswers): JsObject = {
        val encryptedValue: (String, Map[String, EncryptedValue], Instant) =
          ModelEncryption.encryptUserAnswers(userAnswers)
        Json.obj(
          "id"          -> encryptedValue._1,
          "data"        -> encryptedValue._2,
          "lastUpdated" -> encryptedValue._3
        )
      }
    }
    def format(implicit encryption: Encryption): OFormat[UserAnswers] = OFormat(reads, writes)
  }
}
