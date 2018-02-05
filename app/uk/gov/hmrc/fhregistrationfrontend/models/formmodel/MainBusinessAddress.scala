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

package uk.gov.hmrc.fhregistrationfrontend.models.formmodel

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, OFormat}


case class MainBusinessAddress (
  period: String,
  hasOtherAddress: Option[Boolean],
  address: Option[AddressModel]
) extends FormDetails

object MainBusinessAddress {

  implicit val format: OFormat[MainBusinessAddress] = Json.format[MainBusinessAddress]

  def mainBusinessAddressForm = Form(
    mapping(
      "timeAtCurrentAddress" -> nonEmptyText,
      "hasOtherAddress" -> optional(of(CustomFormatters.requiredBooleanFormatter)),
      "previousAddress" -> optional(AddressModel.addressMapping)
    )(MainBusinessAddress.apply)(MainBusinessAddress.unapply)
  )

}



