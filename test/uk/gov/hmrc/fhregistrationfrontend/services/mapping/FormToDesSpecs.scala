package uk.gov.hmrc.fhregistrationfrontend.services.mapping

import com.eclipsesource.schema._
import org.apache.commons.io.FilenameUtils
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.des.SubScriptionCreate
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.data.{LtdLargeUk, LtdMinimum, LtdMinimumInternational}
import uk.gov.hmrc.play.test.UnitSpec

import scala.xml.XML

class FormToDesSpecs extends UnitSpec {

  val schemaAsJson = Json parse getClass.getResourceAsStream("/des/subscription-create.schema.json")
  val schema = Json.fromJson[SchemaType](schemaAsJson).get
  val validator = new SchemaValidator().validate(schema) _
  val service = new FormToDes()

  val brd: BusinessRegistrationDetails = Json
    .parse(getClass.getResourceAsStream("/models/business-registration-details-limited-company.json"))
    .as[BusinessRegistrationDetails]

  "Limited company submission service" should {
    "Create a correct json for fhdds-limited-company-minimum.xml" in {
      val submission = SubScriptionCreate(
        "Create",
        service.limitedCompanySubmission(brd, LtdMinimum.application, LtdMinimum.declaration))

      validatesFor(submission, "fhdds-limited-company-minimum.xml")
    }

    "Create a correct json for fhdds-limited-company-minimum-international.xml" in {
      val submission = SubScriptionCreate(
        "Create",
        service.limitedCompanySubmission(brd, LtdMinimumInternational.application, LtdMinimumInternational.declaration))

    validatesFor(submission, "fhdds-limited-company-minimum-international.xml")

      submission.subScriptionCreate.contactDetail.address.map(_.countryCode) shouldEqual Some("BG")
      submission.subScriptionCreate.contactDetail.address.flatMap(_.line4) shouldEqual Some("Bulgaria")
    }


    "Create a correct json for fhdds-limited-company-large-uk.xml" in {
      val submission = SubScriptionCreate(
        "Create",
        service.limitedCompanySubmission(brd, LtdLargeUk.application, LtdLargeUk.declaration))


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
    val resource = getClass.getResourceAsStream(s"/json/valid/limited-company/$baseName.json")
    Json.parse(resource).as[SubScriptionCreate]
  }


}
