/*
 * Copyright 2021 HM Revenue & Customs
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

case class SoleProprietor(tradingName: Option[String], identification: SoleProprietorIdentification)

object SoleProprietor {
  implicit val format = Json.format[SoleProprietor]
}

case class NonProprietor(tradingName: Option[String], identification: NonProprietorIdentification)

object NonProprietor {
  implicit val format = Json.format[NonProprietor]
}

case class PartnerDetail(
  entityType: String,
  partnerAddress: Address,
  partnerTypeDetail: PartnerType,
  modification: Option[Modification]
)

object PartnerDetail {
  implicit val format = Json.format[PartnerDetail]
}

case class Partnership(numbersOfPartners: String, partnerDetails: List[PartnerDetail])

object Partnership {
  implicit val format = Json.format[Partnership]
}

case class IncorporationDetails(companyRegistrationNumber: Option[String], dateOfIncorporation: Option[LocalDate])

object IncorporationDetails extends DateTimeFormat {

  implicit val format = Json.format[IncorporationDetails]
}

case class LimitedLiabilityPartnershipCorporateBody(
  groupRepresentativeJoinDate: Option[LocalDate],
  incorporationDetails: IncorporationDetails)

object LimitedLiabilityPartnershipCorporateBody extends DateTimeFormat {

  implicit val format = Json.format[LimitedLiabilityPartnershipCorporateBody]
}
