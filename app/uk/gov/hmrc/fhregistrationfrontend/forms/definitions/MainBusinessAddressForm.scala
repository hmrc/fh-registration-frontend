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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{address, oneOf, yesOrNo}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl.MappingsApi.{MappingOps, MappingWithKeyOps}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.MainBusinessAddress

object MainBusinessAddressForm {

  val timeAtCurrentAddressKey = "timeAtCurrentAddress"
  val previousAddressKey = "previousAddress"
  val mainPreviousAddressKey = "mainPreviousAddressUK_previousAddress"

  val timeAtCurrentAddressMapping = timeAtCurrentAddressKey → oneOf(MainBusinessAddress.TimeAtCurrentAddressOptions)
  val previousAddressMapping = previousAddressKey → (yesOrNo onlyWhen (timeAtCurrentAddressMapping is "Less than 3 years"))
  val mainPreviousAddressMapping = mainPreviousAddressKey → (address onlyWhen (previousAddressMapping is Some(true)))

  val mainBusinessAddressForm = Form(
    mapping(
      timeAtCurrentAddressMapping,
      previousAddressMapping,
      mainPreviousAddressMapping
    )(MainBusinessAddress.apply)(MainBusinessAddress.unapply)
  )


}

