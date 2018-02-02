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

import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import play.api.libs.json.{Json, OFormat}

case class BusinessAddress(
  addressLine1: String,
  addressLine2: String,
  addressLine3: Option[String] = None,
  addressLine4: Option[String] = None,
  postcode: String,
  countryCode: Option[String] = Some("GB")
)

object BusinessAddress {
  implicit val addressFormat: OFormat[BusinessAddress] = Json.format[BusinessAddress]

  def addressMapping: Mapping[BusinessAddress] =
    mapping(
      "addressLine1" -> nonEmptyText,
      "addressLine2" -> nonEmptyText,
      "addressLine3" -> optional(nonEmptyText),
      "addressLine4" -> optional(nonEmptyText),
      "postcode" -> nonEmptyText,
      "countryCode" -> optional(nonEmptyText)
    )(BusinessAddress.apply)(BusinessAddress.unapply)

}

case class MainBusinessAddress(
  period: String,
  hasOtherAddress: Option[Boolean],
  address: Option[BusinessAddress])

object MainBusinessAddress {

  implicit val format: OFormat[MainBusinessAddress] = Json.format[MainBusinessAddress]

  def mainBusinessAddressForm = Form(
    mapping(
      "period" -> nonEmptyText,
      "hasOtherAddress" -> optional(of(CustomFormatters.requiredBooleanFormatter)),
      "address" -> optional(BusinessAddress.addressMapping)
    )(MainBusinessAddress.apply)(MainBusinessAddress.unapply)
  )

  def hideField(mainBusinessAddress: Form[MainBusinessAddress]): String = {
    if (mainBusinessAddress("period").hasErrors) "hidden"
    else {
      mainBusinessAddress.value match {
        case Some(v) => if (v.period == "Less than 3 years") "" else "hidden"
        case _ => "hidden"
      }
    }
  }

  def hideAddressField(mainBusinessAddress: Form[MainBusinessAddress]): String = {
    if (mainBusinessAddress("hasOtherAddress").hasErrors) ""
    else {
      mainBusinessAddress.value match {
        case Some(v) => if (v.hasOtherAddress == "true") "" else "hidden"
        case _ => "hidden"
      }
    }
  }

}



