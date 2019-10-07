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

package uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal

import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.FormSpecsHelper
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class WithdrawalReasonFormSpec extends UnitSpec with FormSpecsHelper[WithdrawalReason] {

  override def form: Form[WithdrawalReason] = WithdrawalReasonForm.withdrawalReasonForm
  import WithdrawalReasonForm._

  "Withdrawal reason form" should {
    "Fail if reason is not provided" in {
      formDataHasErrors(Map.empty, Seq(
        reasonKey → "error.required"
      ))
    }

    "Fail if reason is not recognized" in {
      formDataHasErrors(
        Map(reasonKey → "some strange reason"),
        Seq(reasonKey → "error.invalid"))
    }

    "Fail if reason is not 'other' and description is not provided" in {
      formDataHasErrors(
        Map(reasonKey → "Other"),
        Seq(reasonOtherKey → "error.required"))
    }

    "Parse the reason" in {
      dataFromValidForm(Map(reasonKey → "Applied in Error")).withdrawalReason shouldBe WithdrawalReasonEnum.AppliedInError
    }

    "Parse reason other and the description" in {
      val parsed = dataFromValidForm(Map(
        reasonKey → "Other",
        reasonOtherKey → "Some private reason"
      ))
      parsed.withdrawalReason shouldBe WithdrawalReasonEnum.Other
      parsed.withdrawalReasonOther shouldBe Some("Some private reason")
    }

    "Load the data" in {
      val data = WithdrawalReason(WithdrawalReasonEnum.Other, Some("Private reason"))
      form.fill(data).value shouldBe Some(data)
    }


  }

}
