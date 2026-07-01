/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import org.scalatestplus.mockito.MockitoSugar
import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class EoriNumberFormSpecs extends UnitSpec with MockitoSugar {

  val form: Form[String] = ImportingActivitiesForm.eoriNumberOnlyForm
  val field = ImportingActivitiesForm.eoriNumberKey

  "EoriNumberForm" should {

    "reject empty EORI number" in {
      val result = form.bind(Map(field -> ""))
      val errors = result.errors.map(_.message)

      errors should contain("error.required")
    }

    "reject EORI numbers with illegal characters" in {
      val invalidInputs = Seq(
        "as'f",
        "*%",
        "123456789{",
        "GB12@3456"
      )

      invalidInputs.foreach { eori =>
        val result = form.bind(Map(field -> eori))
        val errors = result.errors.map(_.message)

        errors should contain("error.pattern")
      }
    }

    "reject EORI numbers that exceed maximum length" in {
      val tooLong = "GB" + ("1" * 20) // > 15 chars after transform

      val result = form.bind(Map(field -> tooLong))
      val errors = result.errors.map(_.message)

      errors should contain("error.pattern")
    }

    "accept valid EORI numbers" in {
      val validInputs = Seq(
        "GB123456789000",
        "XI123456789000",
        "GB12 345 678 9000",
        "gb123456789000"
      )

      validInputs.foreach { eori =>
        val result = form.bind(Map(field -> eori))
        result.errors shouldBe empty
      }
    }

    "reject missing EORI selection" in {
      val result = ImportingActivitiesForm.hasEoriForm.bind(Map.empty)
      val errors = result.errors.map(_.message)

      errors should contain("error.required")
    }
  }
}
