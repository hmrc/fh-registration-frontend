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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.fhregistrationfrontend.controllers.ControllerSpecWithGuiceApp
import uk.gov.hmrc.fhregistrationfrontend.services.Encryption

import java.time.Instant

class ModelEncryptionSpec extends ControllerSpecWithGuiceApp {

  implicit val encryption: Encryption = app.injector.instanceOf[Encryption]

  "encryptUserAnswers" should {
    "encrypt userAnswers" in {
      val userAnswers = UserAnswers("id", None, Json.obj("foo" -> "bar"), Instant.ofEpochSecond(1))

      val result = ModelEncryption.encryptUserAnswers(userAnswers)
      result._1 mustBe userAnswers.id
      Json.parse(encryption.crypto.decrypt(result._2, userAnswers.id)).as[JsObject] mustBe userAnswers.data
      result._3 mustBe userAnswers.lastUpdated
    }
  }
  "decryptUserAnswers" should {
    "decrypt userAnswers in tuple form" in {
      val userAnswers = UserAnswers("id", None, Json.obj("foo" -> "bar"), Instant.ofEpochSecond(1))

      val result = ModelEncryption.decryptUserAnswers(
        userAnswers.id,
        encryption.crypto.encrypt(userAnswers.data.toString(), userAnswers.id),
        userAnswers.lastUpdated
      )
      result mustBe userAnswers
    }
  }
}
