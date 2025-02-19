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

import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{`enum`, withdrwalReason}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl.MappingsApi.{MappingOps, MappingWithKeyOps}

object WithdrawalReasonForm {

  val reasonKey = "reason"
  val reasonOtherKey = "reasonOtherText"

  val reasonMapping = reasonKey           -> `enum`(WithdrawalReasonEnum)
  val reasonOtherMapping = reasonOtherKey -> (withdrwalReason onlyWhen (reasonMapping is WithdrawalReasonEnum.Other))

  val withdrawalReasonForm = Form(
    mapping(
      reasonMapping,
      reasonOtherMapping
    )(WithdrawalReason.apply)(o => Some(Tuple.fromProductTyped(o)))
  )

}
