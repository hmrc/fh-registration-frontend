/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.services.mapping

import org.apache.commons.io.FilenameUtils
import play.api.libs.json.{JsValue, Json}
import com.eclipsesource.schema._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.LimitedCompanyApplication
import uk.gov.hmrc.fhregistrationfrontend.models.des.{SubscriptionDisplay, SubscriptionDisplayWrapper}
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.data.{LtdLargeUk, LtdMinimum, LtdMinimumInternational}
import uk.gov.hmrc.play.test.UnitSpec

class DesToFormSpec extends UnitSpec {

  val schemaAsJson = Json parse getClass.getResourceAsStream("/des/subscription-display.schema.json")
  val schema = Json.fromJson[SchemaType](schemaAsJson).get
  val validator = new SchemaValidator().validate(schema) _
  val service = new DesToForm()

  "Des to form" should {
    "Correctly load limited-company-minimum" in {
      validatesFor("fhdds-limited-company-minimum", LtdMinimum.application)
    }

    "Correctly load limited-company-minimum-international" in {
      validatesFor("fhdds-limited-company-minimum-international", LtdMinimumInternational.application)
    }

    "Correctly load limited-company-large-uk" in {
      validatesFor("fhdds-limited-company-large-uk", LtdLargeUk.application)
    }
  }


  def validatesFor(file: String, application: LimitedCompanyApplication) = {
    loadDesDataFile(file)
//    val loadedApplication = service limitedCompanySubmission ???
//    loadedApplication shouldEqual application.mainBusinessAddress
  }

  def loadDesDataFile(file: String): SubscriptionDisplay = {
    val baseName = FilenameUtils getBaseName file
    val resource = getClass.getResourceAsStream(s"/json/valid/display/limited-company/$baseName.json")
    val validationResult = validator(Json parse resource)
    validationResult .fold(
      invalid = {errors ⇒ println(errors.toJson)},
      valid = {v ⇒ v}
    )

    validationResult.isSuccess shouldEqual true

    validationResult.get.as[SubscriptionDisplayWrapper].subScriptionDisplay

  }
}
