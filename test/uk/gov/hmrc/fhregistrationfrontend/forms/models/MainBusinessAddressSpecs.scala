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

package uk.gov.hmrc.fhregistrationfrontend.forms.models

import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.MainBusinessAddressForm
import uk.gov.hmrc.play.test.UnitSpec

class MainBusinessAddressSpecs extends UnitSpec {
  val form = MainBusinessAddressForm.mainBusinessAddressForm

  "MainBusinessAddress form binding" should {

    "validate a form over three years" in {
      val data = Map(
        "timeAtCurrentAddress" -> "5 to 10 years"
      )
      val result = form.bind(data).value.get
      result shouldBe MainBusinessAddress("5 to 10 years", None, None)
    }

    "validate a form lese than three years but without previous address" in {
      val data = Map(
        "timeAtCurrentAddress" -> "Less than 3 years",
        "previousAddress" → "false"
      )
      val result = form.bind(data).value.get
      result shouldBe MainBusinessAddress("Less than 3 years", Some(false), None)
    }

    "validate a form less than three years and with previous address" in {
      val data = Map(
        "timeAtCurrentAddress" -> "Less than 3 years",
        "previousAddress" → "true",
        "mainPreviousAddressUK_previousAddress.Line1" → "line1",
        "mainPreviousAddressUK_previousAddress.Line2" → "line2",
        "mainPreviousAddressUK_previousAddress.postcode" → "postcode",
        "mainPreviousAddressUK_previousAddress.countryCode" → "countryCode"
      )
      val result = form.bind(data).value.get
      result shouldBe MainBusinessAddress(
        "Less than 3 years",
        Some(true),
        Some(Address("line1", "line2", None, None, "postcode", Some("countryCode"))))
    }
  }

  "MainBusinessAddress form unbinding" should {
    "unbind a form with address" in {
      val testForm = form.fill(
        MainBusinessAddress(
          "Less than 3 years",
          Some(true),
          Some(Address("line1", "line2", None, None, "postcode", Some("countryCode")))
        )
      )
      testForm.data shouldBe Map(
        "timeAtCurrentAddress" -> "Less than 3 years",
        "previousAddress" → "true",
        "mainPreviousAddressUK_previousAddress.Line1" → "line1",
        "mainPreviousAddressUK_previousAddress.Line2" → "line2",
        "mainPreviousAddressUK_previousAddress.postcode" → "postcode",
        "mainPreviousAddressUK_previousAddress.countryCode" → "countryCode"
      )
    }

  }
}
