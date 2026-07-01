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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import uk.gov.hmrc.fhregistrationfrontend.forms.models.VatNumber
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class VatNumberFormSpecs extends UnitSpec {

  val form = VatNumberForm.vatNumberForm

  "valid VatNumberForm" should {

    "Bind w/o vat number" in {
      val data = Map(
        "vatNumber_yesNo" -> "false"
      )

      form.bind(data).get shouldBe VatNumber(false, None)
    }

    "Bind with vat number" in {
      val data = Map(
        "vatNumber_yesNo" -> "true",
        "vatNumber_value" -> "123456789"
      )

      form.bind(data).get shouldBe VatNumber(true, Some("123456789"))
    }

    "reject VAT numbers with illegal characters" in {
      val invalidInputs = Seq(
        "@@@",
        "12{456789",
        "GB12#56789",
        "GB12 56789",
        "12345A789",
        "GB12345A789"
      )

      invalidInputs.foreach { vat =>
        val result = form.bind(Map("vatNumber_yesNo" -> "true", "vatNumber_value" -> vat))
        val errors = result.errors.flatMap(e => e.messages.map(m => e.key -> m))
        errors should contain("vatNumber_value" -> "error.pattern")
      }
    }

    "reject VAT numbers with invalid length" in {
      val invalidLengths = Seq(
        "12345678",
        "1234567890",
        "GB12345678",
        "GB1234567890"
      )

      invalidLengths.foreach { vat =>
        val result = form.bind(Map("vatNumber_yesNo" -> "true", "vatNumber_value" -> vat))
        val errors = result.errors.flatMap(e => e.messages.map(m => e.key -> m))
        errors should contain("vatNumber_value" -> "error.pattern")
      }
    }

    "accept valid VAT numbers" in {
      val validInputs = Seq(
        "123456789",
        "GB123456789",
        "gb123456789"
      )

      validInputs.foreach { vat =>
        val result = form.bind(Map("vatNumber_yesNo" -> "true", "vatNumber_value" -> vat))
        result.errors shouldBe empty
      }
    }

  }

}
