/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.Json

case class IdentificationBusiness(vatRegistrationNumber: Option[String], uniqueTaxpayerReference: Option[String])

object IdentificationBusiness {
  implicit val format = Json.format[IdentificationBusiness]
}

case class IncorporationDetail(companyRegistrationNumber: Option[String], dateOfIncorporation: Option[LocalDate])

object IncorporationDetail extends DateTimeFormat {

  implicit val format = Json.format[IncorporationDetail]
}

case class MemberDetail(
  names: CompanyName,
  incorporationDetail: IncorporationDetail,
  identification: IdentificationBusiness,
  groupJoiningDate: Option[LocalDate],
  address: Address,
  modification: Modification)

object MemberDetail {
  implicit val format = Json.format[MemberDetail]
}
