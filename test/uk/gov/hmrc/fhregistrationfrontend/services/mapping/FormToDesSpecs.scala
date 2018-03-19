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

import java.time.LocalDate

import com.eclipsesource.schema._
import org.apache.commons.io.FilenameUtils
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.des.{SubScriptionCreate, Subscription}
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.data._
import uk.gov.hmrc.play.test.UnitSpec


class FormToDesSpecs extends UnitSpec {

  val schemaAsJson = Json parse getClass.getResourceAsStream("/des/subscription-create.schema.json")
  val schema = Json.fromJson[SchemaType](schemaAsJson).get
  val validator = new SchemaValidator().validate(schema) _
  val service = new FormToDesImpl()

  def brd(fileName: String): BusinessRegistrationDetails = Json
    .parse(getClass.getResourceAsStream(s"/models/$fileName"))
    .as[BusinessRegistrationDetails]

  "Diff changeIndicators service" should {
    "Check the different between two subscriptions and creates a new json amendment payload for DES" when {
      "For Corporate Body Update" in {
        val originalSubscription: Subscription =
          service.limitedCompanySubmission(
            brd("business-registration-details-limited-company.json"),
            LtdLargeUk.application(),
            LtdLargeUk.declaration)

        val amendedSubscription: Subscription =
          service.limitedCompanySubmission(
            brd("business-registration-details-limited-company.json"),
            LtdLargeUkNew.application(),
            LtdLargeUkNew.declaration)

        val changeIndicators = Diff.changeIndicators(originalSubscription, amendedSubscription)

        val submission = SubScriptionCreate(
          "Update",
          amendedSubscription,
          Some(changeIndicators)
        )

        validatesFor(submission, "fhdds-limited-company-large-uk-amendment", "limited-company")
      }

      "For Sole Proprietor Update" in {
        val originalSubscription: Subscription =
          service.soleProprietorCompanySubmission(
            brd("business-registration-details-sole-trader.json"),
            SPLargeUk.application(),
            SPLargeUk.declaration)

        val amendedSubscription: Subscription =
          service.soleProprietorCompanySubmission(
            brd("business-registration-details-sole-trader.json"),
            SPLargeUkNew.application(),
            SPLargeUkNew.declaration)

        val changeIndicators = Diff.changeIndicators(originalSubscription, amendedSubscription)

        val submission = SubScriptionCreate(
          "Update",
          amendedSubscription,
          Some(changeIndicators)
        )

        validatesFor(submission, "sole-proprietor-large-uk-amendment", "sole-proprietor")
      }

      "For Partnership Update" in {
        val originalSubscription: Subscription =
          service.partnership(
            brd("business-registration-details-partnership.json"),
            PartnershipLargeInt.application(),
            PartnershipLargeInt.declaration)

        val amendedSubscription: Subscription =
          service.partnership(
            brd("business-registration-details-partnership.json"),
            PartnershipLargeIntNew.application(),
            PartnershipLargeIntNew.declaration)

        val changeIndicators = Diff.changeIndicators(originalSubscription, amendedSubscription)

        val submission = SubScriptionCreate(
          "Update",
          amendedSubscription,
          Some(changeIndicators)
        )

        validatesFor(submission, "partnership-large-int-amendment", "partnership")
      }
    }

  }

  "Limited company submission service" should {
    "Create a correct json for fhdds-limited-company-minimum" in {
      val submission = SubScriptionCreate(
        "Create",
        service.limitedCompanySubmission(brd("business-registration-details-limited-company.json"), LtdMinimum.application(), LtdMinimum.declaration),
        None)

      validatesFor(submission, "fhdds-limited-company-minimum", "limited-company")
    }
  }

  "Create a correct json for fhdds-limited-company-minimum-international" in {
    val submission = SubScriptionCreate(
      "Create",
      service.limitedCompanySubmission(brd("business-registration-details-limited-company.json"), LtdMinimumInternational.application(), LtdMinimumInternational.declaration),
      None)

    validatesFor(submission, "fhdds-limited-company-minimum-international.xml", "limited-company")

    submission.subScriptionCreate.contactDetail.address.map(_.countryCode) shouldEqual Some("BG")
    submission.subScriptionCreate.contactDetail.address.flatMap(_.line4) shouldEqual Some("Bulgaria")
  }


  "Create a correct json for fhdds-limited-company-large-uk" in {
    val submission = SubScriptionCreate(
      "Create",
      service.limitedCompanySubmission(brd("business-registration-details-limited-company.json"), LtdLargeUk.application(), LtdLargeUk.declaration),
      None
    )

    validatesFor(submission, "fhdds-limited-company-large-uk", "limited-company")
  }

  "Create a correct json for fhdds-limited-company-large-uk-updated" in {
    val submission = SubScriptionCreate(
      "Update",
      service
        .withModificationFlags(true, Some(LocalDate.of(2018, 2, 1)))
        .limitedCompanySubmission(
          brd("business-registration-details-limited-company.json"),
          LtdLargeUkWithModifications.application,
          LtdLargeUkWithModifications.declaration),
      None
    )

    validatesFor(submission, "fhdds-limited-company-large-uk-updated", "limited-company")
  }

  "Sole proprietor submission service" should {
    "Create a correct json for max. data entry" in {
      val submission = SubScriptionCreate(
        "Create",
        service.soleProprietorCompanySubmission(brd("business-registration-details-sole-trader.json"), SPLargeUk.application(), SPLargeUk.declaration),
        None)

      validatesFor(submission, "sole-proprietor-large-uk", "sole-proprietor")
    }

  }

  "Partnership submission service" should {
    "Create a correct json for max. data entry" in {
      val submission = SubScriptionCreate(
        "Create",
        service.partnership(brd("business-registration-details-partnership.json"), PartnershipLargeInt.application(), PartnershipLargeInt.declaration),
        None)

      validatesFor(submission, "partnership-large-int", "partnership")
    }
  }

  def validatesFor(subscrtiptionCreate: SubScriptionCreate, file: String, entityPath: String) = {

    val json: JsValue = Json.toJson(subscrtiptionCreate)

    val validationResult = validator(json)
    validationResult.fold(
      invalid = {errors ⇒ println(errors.toJson)},
      valid = {v ⇒ }
    )

    validationResult.isSuccess shouldEqual true

    val expected = loadExpectedSubscriptionForFile(file, entityPath)
    subscrtiptionCreate shouldEqual expected

    subscrtiptionCreate
  }

  def loadExpectedSubscriptionForFile(file: String, entityPath: String): SubScriptionCreate = {
    val baseName = FilenameUtils getBaseName file
    val resource = getClass.getResourceAsStream(s"/json/valid/submission/$entityPath/$baseName.json")
    Json.parse(resource).as[SubScriptionCreate]
  }

}
