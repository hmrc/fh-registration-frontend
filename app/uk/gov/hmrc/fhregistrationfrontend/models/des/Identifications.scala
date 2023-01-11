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

import play.api.libs.json._

case class IndividualIdentification(
  passportNumber: Option[String],
  nationalIdNumber: Option[String],
  nino: Option[String])

case class CompanyIdentification(
  vatRegistrationNumber: Option[String],
  uniqueTaxpayerReference: Option[String],
  companyRegistrationNumber: Option[String])

case class SoleProprietorIdentification(
  nino: Option[String],
  vatRegistrationNumber: Option[String],
  uniqueTaxpayerReference: Option[String])

case class NonProprietorIdentification(vatRegistrationNumber: Option[String], uniqueTaxpayerReference: Option[String])

object IndividualIdentification {
  implicit val format = Json.format[IndividualIdentification]
}

object CompanyIdentification {
  implicit val format = Json.format[CompanyIdentification]
}

object SoleProprietorIdentification {
  implicit val format = Json.format[SoleProprietorIdentification]
}

object NonProprietorIdentification {
  implicit val format = Json.format[NonProprietorIdentification]
}
