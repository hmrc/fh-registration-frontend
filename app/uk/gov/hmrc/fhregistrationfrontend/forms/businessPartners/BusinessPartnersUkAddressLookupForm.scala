/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.forms.businessPartners

import play.api.data.Forms.{ignored, mapping, optional}
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{addressLine, postcode}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, UkAddressLookup}

object BusinessPartnersUkAddressLookupForm {

  val addressLineKey = "partnerAddressLine"
  val postcodeKey = "partnerPostcode"
  val addressLookupKey = "addresses"

  val addressLineMapping: (String, Mapping[Option[String]]) =
    addressLineKey -> optional(addressLine)
  val postcodeMapping: (String, Mapping[String]) =
    postcodeKey -> postcode
  val addressLookupMapping: (String, Mapping[Map[String, Address]]) =
    addressLookupKey -> ignored(Map.empty[String, Address])

  val businessPartnersUkAddressLookupForm: Form[UkAddressLookup] = Form(
    mapping(addressLineMapping, postcodeMapping, addressLookupMapping)(UkAddressLookup.apply)(UkAddressLookup.unapply)
  )

}
