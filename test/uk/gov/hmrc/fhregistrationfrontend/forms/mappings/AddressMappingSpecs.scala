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

package uk.gov.hmrc.fhregistrationfrontend.forms.mappings

import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class AddressMappingSpecs extends UnitSpec with MappingSpecsHelper[Address] {

  override val mapping = Mappings.address

  val validAddressShort = Map(
    "Line1"    -> "line one",
    "Line2"    -> "line two",
    "postcode" -> "AA11 1AA"
  )

  val validAddressLong = Map(
    "Line1"    -> "line one",
    "Line2"    -> "line two",
    "Line3"    -> "line three",
    "Line4"    -> "line four",
    "postcode" -> "AA11 1AA"
  )

  "Address mapping " should {
    "reject missing mandatory" in {
      formDataHasErrors(
        Map.empty,
        List("Line1" -> "error.required", "postcode" -> "error.required")
      )
    }

    "reject invalid address lines" in {
      formDataHasErrors(
        Map(
          "Line1"    -> "", // too short
          "Line2"    -> Array.fill(36)('a').mkString, // too long
          "Line3"    -> "street 1 #", // invalid char
          "postcode" -> "123123"
        ),
        List(
          "Line1"    -> "error.pattern",
          "Line2"    -> "error.pattern",
          "Line3"    -> "error.pattern",
          "postcode" -> "error.pattern"
        )
      )
    }

    "accept valid short" in {
      val data = dataFromValidForm(validAddressShort)
      data.addressLine1 shouldBe "line one"
      data.addressLine2 shouldBe Some("line two")
      data.addressLine3 shouldBe None
      data.addressLine4 shouldBe None
      data.postcode shouldBe "AA11 1AA"
      data.countryCode shouldBe None
    }

    "accept valid long" in {
      val data = dataFromValidForm(validAddressLong)
      data.addressLine1 shouldBe "line one"
      data.addressLine2 shouldBe Some("line two")
      data.addressLine3 shouldBe Some("line three")
      data.addressLine4 shouldBe Some("line four")
      data.postcode shouldBe "AA11 1AA"
      data.countryCode shouldBe None
    }
  }

}
