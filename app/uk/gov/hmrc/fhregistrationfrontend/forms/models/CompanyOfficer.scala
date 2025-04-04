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

package uk.gov.hmrc.fhregistrationfrontend.forms.models

import play.api.libs.json._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.CompanyOfficerType._

//TODO add enum for role
case class CompanyOfficer(
  officialType: CompanyOfficialType,
  identification: CompanyOfficerIdentification
)

sealed trait CompanyOfficerIdentification

case class CompanyOfficerIndividual(
  firstName: String,
  lastName: String,
  hasNino: Boolean,
  nino: Option[String],
  hasPassportNumber: Option[Boolean],
  passport: Option[String],
  nationalId: Option[String],
  role: String
) extends CompanyOfficerIdentification

case class CompanyOfficerCompany(
  companyName: String,
  hasVat: Boolean,
  vat: Option[String],
  crn: Option[String],
  role: String
) extends CompanyOfficerIdentification

object CompanyOfficer {
  implicit val companyOfficerCompanyFormat: OFormat[CompanyOfficerCompany] = Json.format[CompanyOfficerCompany]
  implicit val companyOfficerIndividualFormat: OFormat[CompanyOfficerIndividual] = Json.format[CompanyOfficerIndividual]

  implicit val companyOfficerIdentificationWrites: Writes[CompanyOfficerIdentification] =
    new Writes[CompanyOfficerIdentification] {
      override def writes(o: CompanyOfficerIdentification): JsValue =
        o match {
          case i: CompanyOfficerIndividual => Json toJson i
          case c: CompanyOfficerCompany    => Json toJson c
        }
    }

  implicit val reads: Reads[CompanyOfficer] = new Reads[CompanyOfficer] {
    override def reads(value: JsValue): JsResult[CompanyOfficer] =
      value.validate[JsObject].flatMap { json =>
        (json \ "officialType") match {
          case JsDefined(JsString(t)) if t == Individual.toString =>
            (json \ "identification").validate[CompanyOfficerIndividual].map(CompanyOfficer(Individual, _))
          case JsDefined(JsString(t)) if t == Company.toString =>
            (json \ "identification").validate[CompanyOfficerCompany].map(CompanyOfficer(Company, _))
          case e: JsError => e
          case _          => JsError("unknown official type")
        }
      }
  }

  implicit val companyOfficerIdentificationReads: Reads[CompanyOfficerIdentification] = Reads { json =>
    (json \ "companyName").validateOpt[String].flatMap {
      case Some(_) => json.validate[CompanyOfficerCompany]
      case None    => json.validate[CompanyOfficerIndividual]
    }
  }

  implicit val companyOfficerIdentificationFormat: Format[CompanyOfficerIdentification] =
    Format(companyOfficerIdentificationReads, companyOfficerIdentificationWrites)
  implicit val companyOfficerFormat: OFormat[CompanyOfficer] = Json.format[CompanyOfficer]

  def getVatNumber(companyOfficer: CompanyOfficer): Option[String] = companyOfficer.identification match {
    case co: CompanyOfficerCompany   => co.vat
    case _: CompanyOfficerIndividual => None
  }
}
