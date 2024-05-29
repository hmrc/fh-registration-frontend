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

import play.api.libs.json.{Json, OFormat}

case class EORINumberType(EORIVat: Option[String], EORINonVat: Option[String], goodsImportedOutEORI: Option[Boolean])

object EORINumberType {
  implicit val format: OFormat[EORINumberType] = Json.format[EORINumberType]
}

case class PartnerCorporateBody(numberOfOtherOfficials: String, companyOfficials: Option[List[CompanyOfficial]])

object PartnerCorporateBody {
  implicit val format: OFormat[PartnerCorporateBody] = Json.format[PartnerCorporateBody]
}

case class AllOtherInformation(
  numberOfCustomers: String,
  doesEORIExist: Boolean,
  EORINumber: Option[EORINumberType],
  numberOfpremises: String,
  premises: Option[List[Premises]])

object AllOtherInformation {
  implicit val format: OFormat[AllOtherInformation] = Json.format[AllOtherInformation]
}

case class AdditionalBusinessInformationwithType(
  partnerCorporateBody: Option[PartnerCorporateBody],
  allOtherInformation: AllOtherInformation)

object AdditionalBusinessInformationwithType {
  implicit val format: OFormat[AdditionalBusinessInformationwithType] =
    Json.format[AdditionalBusinessInformationwithType]
}
