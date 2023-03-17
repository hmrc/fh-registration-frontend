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

import uk.gov.hmrc.fhregistrationfrontend.forms.models.NationalInsuranceNumber
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class NationalInsuranceNumberFormSpec extends UnitSpec {
  val form = NationalInsuranceNumberForm.nationalInsuranceNumberForm

  "valid Nino" should {
    "bind without the nino" in {
      val data = Map(
        "nationalInsuranceNumber_yesNo" -> "false"
      )

      form.bind(data).get shouldBe NationalInsuranceNumber(false, None)
    }

    "bind with the nino" in {

      val data = Map(
        "nationalInsuranceNumber_yesNo" -> "true",
        "nationalInsuranceNumber_value" -> "QQ123456C"
      )
      form.bind(data).get shouldBe NationalInsuranceNumber(true, Some("QQ123456C"))
    }
  }
}
