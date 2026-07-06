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

import uk.gov.hmrc.fhregistrationfrontend.forms.models.StoragePremise
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec
import play.api.data.Form

class StoragePremisesFormSpecs extends UnitSpec {

  val form: Form[StoragePremise] = Form(StoragePremisesForm.storagePremiseMapping)
  val addressKey = StoragePremisesForm.storagePremise_addressKey
  val thirdPartyKey = StoragePremisesForm.isThirdPartyKey

  "StoragePremisesForm" should {

    "reject missing mandatory fields" in {
      val data = Map(
        s"$addressKey.Line1"    -> "",
        s"$addressKey.postcode" -> "",
        thirdPartyKey           -> ""
      )

      val result = form.bind(data)
      val errors = result.errors.flatMap(e => e.messages.map(m => e.key -> m))

      errors should contain(s"$addressKey.Line1" -> "error.required")
      errors should contain(s"$addressKey.postcode" -> "error.required")
      errors should contain(thirdPartyKey -> "error.required")

    }

    "reject invalid postcode formats" in {
      val invalidPostcodes = Seq("POGH&FGH", "@@", "POOO21 1221", "12AB AB1")

      invalidPostcodes.foreach { pc =>
        val data = Map(
          s"$addressKey.Line1"    -> "Valid address",
          s"$addressKey.postcode" -> pc,
          thirdPartyKey           -> "true"
        )

        val result = form.bind(data)
        val errors = result.errors.flatMap(e => e.messages.map(m => e.key -> m))

        errors should contain(s"$addressKey.postcode" -> "error.pattern")

      }
    }

    "reject illegal characters in address lines" in {
      val invalidLines = Seq("@-=", "++_=-", "\"break;//")

      invalidLines.foreach { line =>
        val data = Map(
          s"$addressKey.Line1"    -> line,
          s"$addressKey.postcode" -> "AB1 2YZ",
          thirdPartyKey           -> "false"
        )

        val result = form.bind(data)
        val errors = result.errors.flatMap(e => e.messages.map(m => e.key -> m))

        errors should contain(s"$addressKey.Line1" -> "error.pattern")
      }
    }

    "accept valid UK address" in {
      val data = Map(
        s"$addressKey.Line1"    -> "Flat 1",
        s"$addressKey.Line2"    -> "High Street",
        s"$addressKey.Line3"    -> "Newcastle",
        s"$addressKey.Line4"    -> "Tyne and Wear",
        s"$addressKey.postcode" -> "AB1 2YZ",
        thirdPartyKey           -> "true"
      )

      val result = form.bind(data)
      result.errors shouldBe empty

      val premise = result.value.get
      premise.address.postcode shouldBe "AB1 2YZ"
      premise.isThirdParty shouldBe true
    }

    "reject missing selection for other storage premises" in {
      val result = StoragePremisesForm.hasOtherStoragePrmisesForm.bind(Map.empty)
      val errors = result.errors.map(_.message)

      errors should contain("error.required")
    }

  }
}
