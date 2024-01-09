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

import org.apache.commons.lang3.StringUtils
import play.api.data.{Form, Mapping}
import play.api.data.Forms.{mapping, nonEmptyText}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings._
import uk.gov.hmrc.fhregistrationfrontend.forms.models._

object BusinessPartnersChooseAddressForm {

  val chooseAddressKey = "chosenAddress"

  val chooseAddressMapping: Mapping[ChooseAddress] = mapping(
    chooseAddressKey -> nonEmptyText
  )(ChooseAddress.apply)(ChooseAddress.unapply)

  val chooseAddressForm = Form(chooseAddressMapping)

}
