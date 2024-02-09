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

import play.api.libs.json._
import queries.{Gettable, Settable}
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.fhregistrationfrontend.models.ModelEncryption
import uk.gov.hmrc.fhregistrationfrontend.services.Encryption
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import uk.gov.hmrc.crypto.json.CryptoFormats
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.Utr

import java.time.Instant
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
  id: String,
  data: JsObject = Json.obj(),
  lastUpdated: Instant = Instant.now
) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {

    val updatedData = data.setObject(page.path, Json.toJson(value)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(Some(value), updatedAnswers)
    }
  }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap { d =>
      val updatedAnswers = copy(data = d)
      page.cleanup(None, updatedAnswers)
    }
  }
}

object UserAnswers {

  object MongoFormats {
    implicit val cryptEncryptedValueFormats: Format[EncryptedValue] = CryptoFormats.encryptedValueFormat

    import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats.Implicits._

    def reads()(implicit encryption: Encryption): Reads[UserAnswers] =
      (
        (__ \ "_id").read[String] and
          (__ \ "data").read[EncryptedValue] and
          (__ \ "lastUpdated").read[Instant]
      )(ModelEncryption.decryptUserAnswers _)

    def writes(implicit encryption: Encryption): OWrites[UserAnswers] = new OWrites[UserAnswers] {
      override def writes(userAnswers: UserAnswers): JsObject = {
        val encryptedValue: (String, EncryptedValue, Instant) = {
          ModelEncryption.encryptUserAnswers(userAnswers)
        }
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
