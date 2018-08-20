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

package uk.gov.hmrc.fhregistrationfrontend.forms.journeys

import java.time.LocalDate

import org.scalatest.mockito.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.MainBusinessAddressForm
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, MainBusinessAddress}
import uk.gov.hmrc.play.test.UnitSpec

class BasicPageSpecs extends UnitSpec with MockitoSugar {

  val page = Page.mainBusinessAddressPage

  "updatedAddresses" should {
    "return the new address" in {

     val request = FakeRequest().withFormUrlEncodedBody(formData(): _*)

      val newPage = page.parseFromRequest(
        _ ⇒ fail("Should have no errors"),
        page ⇒ page.updatedAddresses.size shouldBe 1
      )(request)
    }

    "return no new address" in {
      val request = FakeRequest().withFormUrlEncodedBody(formData(): _*)
      val data = MainBusinessAddress(
        "Less than 3 years",
        Some(true),
        Some(Address("Other", None, None, None, "AA11 1AA", None, None)),
        Some(LocalDate.now())
      )

      page.withData(data).parseFromRequest(
        _ ⇒ fail("Should have no errors"),
        page ⇒ page.updatedAddresses.size shouldBe 1
      )(request)
    }
  }


  private def formData() = {
    val addressForm = Seq(
      "Line1" → "Some",
      "postcode" → "AA11 1AA"
    ).map { case (k, v) ⇒ s"mainPreviousAddressUK_previousAddress.$k" -> v}

    val dateForm = Seq(
      "day" → "01",
      "month" → "01",
      "year" → "2017"
    ).map { case (k, v) ⇒ s"${MainBusinessAddressForm.previousAddressStartdateKey}.$k" -> v}

    val form = Seq(
      MainBusinessAddressForm.timeAtCurrentAddressKey  → "Less than 3 years",
      MainBusinessAddressForm.previousAddressKey → "true"
    )
    form ++ dateForm ++ addressForm
  }
}
