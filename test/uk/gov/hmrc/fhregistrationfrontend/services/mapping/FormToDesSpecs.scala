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

package uk.gov.hmrc.fhregistrationfrontend.services.mapping

import com.fasterxml.jackson.databind.JsonNode
import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.{ListReportProvider, LogLevel, ProcessingReport}
import com.github.fge.jsonschema.main.JsonSchemaFactory
import org.apache.commons.io.FilenameUtils
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.BusinessPartnerType
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.des.{CompanyAsOfficial, IndividualPartnerType, LimitedLiabilityPartnershipType, PartnershipOrUnIncorporatedBodyPartnerType, SoleProprietorPartnerType, SubScriptionCreate, Subscription}
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.data._
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

import java.io.InputStream
import java.time.LocalDate
import scala.io.Source

class FormToDesSpecs extends UnitSpec {

  val factory = JsonSchemaFactory
    .newBuilder()
    .setReportProvider(new ListReportProvider(LogLevel.ERROR, LogLevel.FATAL))
    .freeze()

  val stream: InputStream = getClass.getResourceAsStream("/des/subscription-create.schema.json")
  val file: String = Source.fromInputStream(stream).mkString

  val schemaJson = JsonLoader.fromString(file)
  val schema = factory.getJsonSchema(schemaJson)

  def validateAgainstSchema(json: JsonNode): ProcessingReport =
    schema.validate(json, true)

  val service = new FormToDesImpl()

  def brd(fileName: String): BusinessRegistrationDetails =
    Json
      .parse(getClass.getResourceAsStream(s"/models/$fileName"))
      .as[BusinessRegistrationDetails]

  "Form for amendment service" should {
    "Compute the differences between initial subscription and the amendment for Corporate Body" in {

      val originalSubscription: Subscription =
        service.limitedCompanySubmission(
          brd("business-registration-details-limited-company.json"),
          LtdLargeUk.verifiedEmail,
          LtdLargeUk.application(),
          LtdLargeUk.declaration
        )

      val amendedSubscription: Subscription =
        service.limitedCompanySubmission(
          brd("business-registration-details-limited-company.json"),
          LtdLargeUkNew.verifiedEmail,
          LtdLargeUkNew.application(),
          LtdLargeUkNew.declaration
        )

      val changeIndicators = Diff.changeIndicators(originalSubscription, amendedSubscription)

      val submission = SubScriptionCreate(
        "Update",
        service
          .withModificationFlags(true, Some(LocalDate.of(2018, 2, 1)))
          .limitedCompanySubmission(
            brd("business-registration-details-limited-company.json"),
            LtdLargeUkNew.verifiedEmail,
            LtdLargeUkNew.application(),
            LtdLargeUkNew.declaration
          ),
        Some(changeIndicators)
      )

      validatesFor(submission, "fhdds-limited-company-large-uk-amendment", "limited-company")
    }

    "Compute the differences between initial subsciption and the amendment for Sole Proprietor Update" in {

      val originalSubscription: Subscription =
        service.soleProprietorCompanySubmission(
          brd("business-registration-details-sole-trader.json"),
          SPLargeUk.verifiedEmail,
          SPLargeUk.application(),
          SPLargeUk.declaration
        )

      val amendedSubscription: Subscription =
        service.soleProprietorCompanySubmission(
          brd("business-registration-details-sole-trader.json"),
          SPLargeUkNew.verifiedEmail,
          SPLargeUkNew.application(),
          SPLargeUkNew.declaration
        )

      val changeIndicators = Diff.changeIndicators(originalSubscription, amendedSubscription)

      val submission = SubScriptionCreate(
        "Update",
        service
          .withModificationFlags(true, Some(LocalDate.of(2018, 2, 1)))
          .soleProprietorCompanySubmission(
            brd("business-registration-details-sole-trader.json"),
            SPLargeUkNew.verifiedEmail,
            SPLargeUkNew.application(),
            SPLargeUkNew.declaration
          ),
        Some(changeIndicators)
      )

      validatesFor(submission, "sole-proprietor-large-uk-amendment", "sole-proprietor")
    }

    "Compute the differences between initial subsciption and the amendment for Partnership Update" in {
      val originalSubscription: Subscription =
        service.partnership(
          brd("business-registration-details-partnership.json"),
          PartnershipLargeInt.verifiedEmail,
          PartnershipLargeInt.application(),
          PartnershipLargeInt.declaration
        )

      val amendedSubscription: Subscription =
        service.partnership(
          brd("business-registration-details-partnership.json"),
          PartnershipLargeIntNew.verifiedEmail,
          PartnershipLargeIntNew.application(),
          PartnershipLargeIntNew.declaration
        )

      val changeIndicators = Diff.changeIndicators(originalSubscription, amendedSubscription)

      val submission = SubScriptionCreate.subscriptionAmend(
        changeIndicators,
        service
          .withModificationFlags(true, Some(LocalDate.of(2018, 2, 1)))
          .partnership(
            brd("business-registration-details-sole-trader.json"),
            PartnershipLargeIntNew.verifiedEmail,
            PartnershipLargeIntNew.application(),
            PartnershipLargeIntNew.declaration
          )
      )

      validatesFor(submission, "partnership-large-int-amendment", "partnership")
    }
  }

  "Limited company submission service" should {
    "Create a correct json for fhdds-limited-company-minimum" in {
      val submission = SubScriptionCreate(
        "Create",
        service.limitedCompanySubmission(
          brd("business-registration-details-limited-company.json"),
          LtdMinimum.verifiedEmail,
          LtdMinimum.application(),
          LtdMinimum.declaration
        ),
        None
      )

      validatesFor(submission, "fhdds-limited-company-minimum", "limited-company")
    }
  }

  "Limited company submission service" should {
    "Create a correct json for fhdds-limited-company-minimum-less-than-three-years" in {
      val submission = SubScriptionCreate(
        "Create",
        service.limitedCompanySubmission(
          brd("business-registration-details-limited-company.json"),
          LtdMinimumLessThanThreeYears.verifiedEmail,
          LtdMinimumLessThanThreeYears.application(),
          LtdMinimumLessThanThreeYears.declaration
        ),
        None
      )

      validatesFor(submission, "fhdds-limited-company-minimum-less-than-three-years", "limited-company")
    }
  }

  "Create a correct json for fhdds-limited-company-minimum-international" in {
    val submission = SubScriptionCreate(
      "Create",
      service.limitedCompanySubmission(
        brd("business-registration-details-limited-company.json"),
        LtdMinimumInternational.verifiedEmail,
        LtdMinimumInternational.application(),
        LtdMinimumInternational.declaration
      ),
      None
    )

    validatesFor(submission, "fhdds-limited-company-minimum-international", "limited-company")

    submission.subScriptionCreate.contactDetail.address.map(_.countryCode) shouldEqual Some("BG")
    submission.subScriptionCreate.contactDetail.address.flatMap(_.line4) shouldEqual Some("Bulgaria")
  }

  "Create a correct json for fhdds-limited-company-large-uk" in {
    val submission = SubScriptionCreate(
      "Create",
      service.limitedCompanySubmission(
        brd("business-registration-details-limited-company.json"),
        LtdLargeUk.verifiedEmail,
        LtdLargeUk.application(),
        LtdLargeUk.declaration
      ),
      None
    )

    validatesFor(submission, "fhdds-limited-company-large-uk", "limited-company")
  }

  "Normalise prefixed VAT numbers for limited company DES submission" in {
    val application = LtdLargeUk.application()
    val prefixedApplication = application.copy(
      vatNumber = VatNumber(true, Some("GB123456789")),
      companyOfficers = application.companyOfficers.updated(
        2,
        CompanyOfficer(
          CompanyOfficerType.Company,
          CompanyOfficerCompany("Some Company", hasVat = true, Some("GB987654321"), None, "Company Secretary")
        )
      )
    )

    val submission = SubScriptionCreate(
      "Create",
      service.limitedCompanySubmission(
        brd("business-registration-details-limited-company.json"),
        LtdLargeUk.verifiedEmail,
        prefixedApplication,
        LtdLargeUk.declaration
      ),
      None
    )

    submission.subScriptionCreate.businessDetail.nonProprietor.flatMap(
      _.identification.vatRegistrationNumber
    ) shouldBe Some("123456789")
    companyOfficialVatNumbers(submission.subScriptionCreate) should contain("987654321")
    validateAgainstSchema(JsonLoader.fromString(Json.toJson(submission).toString)).isSuccess shouldBe true
  }

  "Create a correct json for fhdds-limited-company-large-uk-updated" in {
    val submission = SubScriptionCreate(
      "Update",
      service
        .withModificationFlags(true, Some(LocalDate.of(2018, 2, 1)))
        .limitedCompanySubmission(
          brd("business-registration-details-limited-company.json"),
          LtdLargeUkWithModifications.verifiedEmail,
          LtdLargeUkWithModifications.application,
          LtdLargeUkWithModifications.declaration
        ),
      None
    )

    validatesFor(submission, "fhdds-limited-company-large-uk-updated", "limited-company")
  }

  "Create a correct json for fhdds-limited-company-large-uk-amend-no-premises" in {
    val submission = SubScriptionCreate(
      "Update",
      service
        .withModificationFlags(true, Some(LocalDate.of(2018, 2, 1)))
        .limitedCompanySubmission(
          brd("business-registration-details-limited-company.json"),
          LtdLargeUkAmendNoPremises.verifiedEmail,
          LtdLargeUkAmendNoPremises.application,
          LtdLargeUkAmendNoPremises.declaration
        ),
      None
    )

    validatesFor(submission, "fhdds-limited-company-large-uk-amend-no-premises", "limited-company")
  }

  "Sole proprietor submission service" should {
    "Create a correct json for max. data entry" in {
      val submission = SubScriptionCreate(
        "Create",
        service.soleProprietorCompanySubmission(
          brd("business-registration-details-sole-trader.json"),
          SPLargeUk.verifiedEmail,
          SPLargeUk.application(),
          SPLargeUk.declaration
        ),
        None
      )

      validatesFor(submission, "sole-proprietor-large-uk", "sole-proprietor")
    }

  }

  "Partnership submission service" should {
    "Create a correct json for max. data entry" in {
      val submission = SubScriptionCreate(
        "Create",
        service.partnership(
          brd("business-registration-details-partnership.json"),
          PartnershipLargeInt.verifiedEmail,
          PartnershipLargeInt.application(),
          PartnershipLargeInt.declaration
        ),
        None
      )

      validatesFor(submission, "partnership-large-int", "partnership")
    }

    "Normalise prefixed VAT numbers for business partners before DES submission" in {
      val application = PartnershipLargeInt.application()
      val prefixedBusinessPartners = application.businessPartners
        .updated(
          1,
          BusinessPartner(
            BusinessPartnerType.SoleProprietor,
            BusinessPartnerSoleProprietor(
              "ms sole",
              "trader",
              hasTradeName = true,
              Some("dodgy sole trader"),
              hasNino = true,
              Some("AA123231"),
              hasVat = true,
              Some("GB223456789"),
              None,
              Address("sole line one", None, None, Some("sole town"), "AA13 1AA", None, None)
            )
          )
        )
        .updated(
          2,
          BusinessPartner(
            BusinessPartnerType.LimitedLiabilityPartnership,
            BusinessPartnerLimitedLiabilityPartnership(
              "fulfilment llp",
              hasTradeName = true,
              Some("dodgy llp"),
              "SC123456",
              hasVat = true,
              Some("GB323456789"),
              None,
              Address(
                "llp line one",
                Some("llp line two"),
                Some("llp line three"),
                Some("llp town"),
                "AA14 1AA",
                None,
                None
              )
            )
          )
        )

      val submission = SubScriptionCreate(
        "Create",
        service.partnership(
          brd("business-registration-details-partnership.json"),
          PartnershipLargeInt.verifiedEmail,
          application.copy(
            vatNumber = VatNumber(true, Some("GB123456789")),
            businessPartners = prefixedBusinessPartners
          ),
          PartnershipLargeInt.declaration
        ),
        None
      )

      submission.subScriptionCreate.businessDetail.nonProprietor.flatMap(
        _.identification.vatRegistrationNumber
      ) shouldBe Some("123456789")
      partnerVatNumbers(submission.subScriptionCreate) should contain allOf ("223456789", "323456789")
      validateAgainstSchema(JsonLoader.fromString(Json.toJson(submission).toString)).isSuccess shouldBe true
    }
  }

  private def companyOfficialVatNumbers(subscription: Subscription): List[String] =
    subscription.additionalBusinessInformation.partnerCorporateBody
      .flatMap(_.companyOfficials)
      .toList
      .flatten
      .collect { case company: CompanyAsOfficial =>
        company.identification.vatRegistrationNumber
      }
      .flatten

  private def partnerVatNumbers(subscription: Subscription): List[String] =
    subscription.businessDetail.partnership
      .map(_.partnerDetails)
      .getOrElse(Nil)
      .flatMap {
        _.partnerTypeDetail match {
          case partnerType: SoleProprietorPartnerType       => partnerType.identification.vatRegistrationNumber
          case partnerType: LimitedLiabilityPartnershipType => partnerType.identification.vatRegistrationNumber
          case partnerType: PartnershipOrUnIncorporatedBodyPartnerType =>
            partnerType.identification.vatRegistrationNumber
          case _: IndividualPartnerType => None
        }
      }

  def validatesFor(subscrtiptionCreate: SubScriptionCreate, file: String, entityPath: String) = {

    val json: JsValue = Json.toJson(subscrtiptionCreate)
    val jsonNode = JsonLoader.fromString(json.toString)
    val validationResult = validateAgainstSchema(jsonNode)

    validationResult.isSuccess shouldEqual true

    val expected = loadExpectedSubscriptionForFile(file, entityPath)
    subscrtiptionCreate shouldEqual expected
    subscrtiptionCreate
  }

  def loadExpectedSubscriptionForFile(file: String, entityPath: String): SubScriptionCreate = {
    val baseName = FilenameUtils `getBaseName` file
    val resource = getClass.getResourceAsStream(s"/json/valid/submission/$entityPath/$baseName.json")
    Json.parse(resource).as[SubScriptionCreate]
  }
}
