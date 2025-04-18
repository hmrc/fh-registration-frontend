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

package uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal

import play.api.libs.json.{Format, Reads, Writes}
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal

object WithdrawalReasonEnum extends Enumeration {
  type WithdrawalReasonEnum = Value

  val AppliedInError = Value("Applied in Error")
  val NoLongerApplicable = Value("No Longer Applicable")
  val DuplicateApplication = Value("Duplicate Application")
  val Other = Value("Other")

  implicit val withdrawalReasonTypeValueOf: ValueOf[WithdrawalReasonEnum.type] = ValueOf(WithdrawalReasonEnum)

  implicit val format: Format[withdrawal.WithdrawalReasonEnum.Value] = Format(
    Reads.enumNameReads(WithdrawalReasonEnum),
    Writes.enumNameWrites[this.type]
  )

}
