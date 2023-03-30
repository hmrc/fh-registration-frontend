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
import play.api.data.Forms.{mapping, nonEmptyText, optional, text}
import play.api.data.validation.Constraints
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.addressLine
import uk.gov.hmrc.fhregistrationfrontend.forms.models._

object BusinessPartnersEnterAddressForm {

  val enterAddressKey = "enteredAddress"

  def postcode: Mapping[String] =
    nonEmptyText.verifying(Constraints.pattern("^[A-Za-z]{1,2}[0-9][0-9A-Za-z]?\\s*?[0-9][A-Za-z]{2}$".r))

  val enterAddressMapping: Mapping[Address] = mapping(
    "Line1"       -> addressLine,
    "Line2"       -> optional(addressLine),
    "Line3"       -> optional(addressLine),
    "Line4"       -> optional(addressLine),
    "postcode"    -> postcode,
    "countryCode" -> optional(nonEmptyText),
    "lookupId"    -> optional(text).transform(_ filterNot StringUtils.isBlank, (v: Option[String]) => v)
  )(Address.apply)(Address.unapply)

  val chooseAddressForm = Form(enterAddressMapping)

}
