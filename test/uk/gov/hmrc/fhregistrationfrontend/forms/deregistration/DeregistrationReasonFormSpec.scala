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

package uk.gov.hmrc.fhregistrationfrontend.forms.deregistration

import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.FormSpecsHelper
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class DeregistrationReasonFormSpec extends UnitSpec with FormSpecsHelper[DeregistrationReason] {

  override def form: Form[DeregistrationReason] = DeregistrationReasonForm.deregistrationReasonForm

  import DeregistrationReasonForm._

  "Deregistration reason form" should {
    "Fail if reason is not provided" in {
      formDataHasErrors(
        Map.empty,
        Seq(
          reasonKey -> "error.required"
        )
      )
    }

    "Fail if reason is not recognized" in {
      formDataHasErrors(Map(reasonKey -> "some strange reason"), Seq(reasonKey -> "error.invalid"))
    }

    "Fail if reason is not 'other' and description is not provided" in {
      formDataHasErrors(Map(reasonKey -> "Others"), Seq(reasonOtherKey -> "error.required"))
    }

    "Parse the reason" in {
      dataFromValidForm(
        Map(reasonKey -> "CEASES_REGISTERABLE_SCHEME")
      ).deregistrationReason shouldBe DeregistrationReasonEnum.NoLongerNeeded
    }

    "Parse reason other and the description" in {
      val parsed = dataFromValidForm(
        Map(
          reasonKey      -> "Others",
          reasonOtherKey -> "Some private reason"
        )
      )
      parsed.deregistrationReason shouldBe DeregistrationReasonEnum.Other
      parsed.deregistrationReasonOther shouldBe Some("Some private reason")
    }

    "Load the data" in {
      val data = DeregistrationReason(DeregistrationReasonEnum.Other, Some("Private reason"))
      form.fill(data).value shouldBe Some(data)
    }

  }

}
