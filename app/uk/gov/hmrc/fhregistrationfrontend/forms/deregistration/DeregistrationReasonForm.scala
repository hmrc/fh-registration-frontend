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

import play.api.data.{Form, FormError, Forms, Mapping}
import play.api.data.Forms.{mapping, optional, text}
import play.api.data.format.Formatter
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{`enum`, deregistrationReason}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl.MappingsApi.{MappingOps, MappingWithKeyOps}

object DeregistrationReasonForm {

  val reasonKey = "reason"
  val reasonOtherKey = "otherReasonContainer"

  val reasonMapping = reasonKey -> `enum`(DeregistrationReasonEnum)
  val reasonOtherMapping =
    reasonOtherKey -> (deregistrationReason onlyWhen (reasonMapping is DeregistrationReasonEnum.Other))

  val deregistrationReasonForm = Form(
    mapping(
      reasonMapping,
      reasonOtherMapping
    )(DeregistrationReason.apply)(o => Some(Tuple.fromProductTyped(o)))
  )
}
