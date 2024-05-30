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

package uk.gov.hmrc.fhregistrationfrontend.models.des

import java.time.LocalDate
import play.api.libs.json.{Json, OFormat}

case class PreviousOperationalAddressDetail(previousAddress: Address, previousAddressStartdate: LocalDate)

object PreviousOperationalAddressDetail extends DateTimeFormat {
  implicit val format: OFormat[PreviousOperationalAddressDetail] = Json.format[PreviousOperationalAddressDetail]

}

case class PreviousOperationalAddress(
  anyPreviousOperatingAddress: Boolean,
  previousOperationalAddressDetail: Option[List[PreviousOperationalAddressDetail]])

object PreviousOperationalAddress {
  implicit val format: OFormat[PreviousOperationalAddress] = Json.format[PreviousOperationalAddress]
}

case class BusinessAddressForFHDDS(
  currentAddress: Address,
  commonDetails: CommonDetails,
  timeOperatedAtCurrentAddress: String,
  previousOperationalAddress: Option[PreviousOperationalAddress])

object BusinessAddressForFHDDS {
  implicit val format: OFormat[BusinessAddressForFHDDS] = Json.format[BusinessAddressForFHDDS]

  def parseToRequiredString(value: String) = value match {
    case "3-5 years"  => "3 to 5 years"
    case "5-10 years" => "5 to 10 years"
    case _            => value
  }
}
