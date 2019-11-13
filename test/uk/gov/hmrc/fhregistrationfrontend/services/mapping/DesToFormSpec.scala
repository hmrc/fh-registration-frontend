/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.libs.json.Json
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.ListWithTrackedChanges.NoChange
import uk.gov.hmrc.fhregistrationfrontend.models.des.{SubscriptionDisplay, SubscriptionDisplayWrapper}
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.data._
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class DesToFormSpec extends UnitSpec {

  val schemaAsJson = Json parse getClass.getResourceAsStream("/des/subscription-display.schema.json")
  val schema = Json.fromJson[SchemaType](schemaAsJson).get
  val validator = new SchemaValidator().validate(schema) _
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
      case "Corporate Body" ⇒ service limitedCompanyApplication display
      case "Sole Proprietor" ⇒ service soleProprietorApplication display
      case "Partnership" ⇒ service partnershipApplication display
    }
    loadedApplication shouldEqual application
  }

  def loadDesDataFile(filePath: String): SubscriptionDisplay = {
    val resource = getClass.getResourceAsStream(s"/json/valid/display/$filePath.json")
    val validationResult = validator(Json parse resource)
    validationResult.fold(
      invalid = { errors ⇒
        println(errors)
      },
      valid = { v ⇒
        v
      }
    )

    validationResult.isSuccess shouldEqual true
    validationResult.get.as[SubscriptionDisplayWrapper].subScriptionDisplay
  }
}
