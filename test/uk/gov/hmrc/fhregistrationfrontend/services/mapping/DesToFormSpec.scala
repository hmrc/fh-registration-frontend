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
import play.api.libs.json.Json
import uk.gov.hmrc.fhregistrationfrontend.forms.models.ListWithTrackedChanges.NoChange
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.models.des.{SubscriptionDisplay, SubscriptionDisplayWrapper}
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.data._
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

import java.io.InputStream
import scala.io.Source

//TODO: FIX
class DesToFormSpec extends UnitSpec {

  val factory = JsonSchemaFactory
    .newBuilder()
    .setReportProvider(new ListReportProvider(LogLevel.ERROR, LogLevel.FATAL))
    .freeze()

  val stream: InputStream = getClass.getResourceAsStream("/des/subscription-display.schema.json")
  val file: String = Source.fromInputStream(stream).mkString

  val schemaJson = JsonLoader.fromString(file)
  val schema = factory.getJsonSchema(schemaJson)

  def validateAgainstSchema(json: JsonNode): ProcessingReport =
    schema.validate(json, true)
  val service = new DesToFormImpl()

  "Des to form" should {
    "Correctly load limited-company-minimum" in {
      validatesFor("limited-company/fhdds-limited-company-minimum", LtdMinimum.application(NoChange))
    }

    "Correctly load fhdds-limited-company-minimum-less-than-three-years" in {
      validatesFor(
        "limited-company/fhdds-limited-company-minimum-less-than-three-years",
        LtdMinimumLessThanThreeYears.application(NoChange))
    }

    "Correctly load limited-company-minimum-international" in {
      validatesFor(
        "limited-company/fhdds-limited-company-minimum-international",
        LtdMinimumInternational.application(NoChange))
    }

    "Correctly load limited-company-large-uk" in {
      validatesFor("limited-company/fhdds-limited-company-large-uk", LtdLargeUk.application(NoChange))
    }

    "Correctly load sole-proprietor-large-uk" in {
      validatesFor("sole-proprietor/sole-proprietor-large-uk", SPLargeUk.application(NoChange))
    }

    "Correctly load partnership-large-int" in {
      validatesFor("partnership/partnership-large-int", PartnershipLargeInt.application(NoChange))
    }
  }

  def validatesFor(file: String, application: BusinessEntityApplication) = {
    val display = loadDesDataFile(file)
    val loadedApplication = display.organizationType match {
      case "Corporate Body"  => service limitedCompanyApplication display
      case "Sole Proprietor" => service soleProprietorApplication display
      case "Partnership"     => service partnershipApplication display
    }
    loadedApplication shouldEqual application
  }

  def loadDesDataFile(filePath: String): SubscriptionDisplay = {
    val resource = getClass.getResourceAsStream(s"/json/valid/display/$filePath.json")
    val jsonString = Source.fromInputStream(resource).mkString
    val jsonNode = JsonLoader.fromString(jsonString)
    val validationResult = validateAgainstSchema(jsonNode)
    validationResult.isSuccess shouldEqual true
    Json.parse(jsonString).as[SubscriptionDisplayWrapper].subScriptionDisplay
  }
}
