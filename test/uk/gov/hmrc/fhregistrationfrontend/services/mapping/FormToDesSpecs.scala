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

import com.eclipsesource.schema._
import org.apache.commons.io.FilenameUtils
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.des.SubScriptionCreate
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.data.{LtdLargeUk, LtdMinimum, LtdMinimumInternational}
import uk.gov.hmrc.play.test.UnitSpec


class FormToDesSpecs extends UnitSpec {

  val schemaAsJson = Json parse getClass.getResourceAsStream("/des/subscription-create.schema.json")
  val schema = Json.fromJson[SchemaType](schemaAsJson).get
  val validator = new SchemaValidator().validate(schema) _
  val service = new FormToDesImpl()

  val brd: BusinessRegistrationDetails = Json
    .parse(getClass.getResourceAsStream("/models/business-registration-details-limited-company.json"))
    .as[BusinessRegistrationDetails]

  "Limited company submission service" should {
    "Create a correct json for fhdds-limited-company-minimum" in {
      val submission = SubScriptionCreate(
        "Create",
        service.limitedCompanySubmission(brd, LtdMinimum.application, LtdMinimum.declaration),
        None)

      validatesFor(submission, "fhdds-limited-company-minimum")
    }

    "Create a correct json for fhdds-limited-company-minimum-international" in {
      val submission = SubScriptionCreate(
        "Create",
        service.limitedCompanySubmission(brd, LtdMinimumInternational.application, LtdMinimumInternational.declaration),
        None)

    validatesFor(submission, "fhdds-limited-company-minimum-international.xml")

      submission.subScriptionCreate.contactDetail.address.map(_.countryCode) shouldEqual Some("BG")
      submission.subScriptionCreate.contactDetail.address.flatMap(_.line4) shouldEqual Some("Bulgaria")
    }


    "Create a correct json for fhdds-limited-company-large-uk.xml" in {
      val submission = SubScriptionCreate(
        "Create",
        service.limitedCompanySubmission(brd, LtdLargeUk.application, LtdLargeUk.declaration),
        None
      )


      validatesFor(submission, "fhdds-limited-company-large-uk.xml")
    }
  }


  def validatesFor(subscrtiptionCreate: SubScriptionCreate, file: String) = {

    val json: JsValue = Json.toJson(subscrtiptionCreate)

    val validationResult = validator(json)
    validationResult.fold(
      invalid = {errors ⇒ println(errors.toJson)},
      valid = {v ⇒ }
    )

    validationResult.isSuccess shouldEqual true

    val expected = loadExpectedSubscriptionForFile(file)
    subscrtiptionCreate shouldEqual expected


    subscrtiptionCreate
  }

  def loadExpectedSubscriptionForFile(file: String): SubScriptionCreate = {
    val baseName = FilenameUtils getBaseName file
    val resource = getClass.getResourceAsStream(s"/json/valid/submission/limited-company/$baseName.json")
    Json.parse(resource).as[SubScriptionCreate]
  }


}
