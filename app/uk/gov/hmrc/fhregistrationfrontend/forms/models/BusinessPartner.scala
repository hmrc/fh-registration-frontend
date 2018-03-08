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

package uk.gov.hmrc.fhregistrationfrontend.forms.models

import play.api.libs.json._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnersType._

case class BusinessPartner(
  businessPartnerType: BusinessPartnerTypes,
  identification: BusinessPartnerIdentification
)

sealed trait BusinessPartnerIdentification

case class BusinessPartnerIndividual(
  firstName: String,
  lastName: String,
  hasNino: Boolean,
  nino: Option[String],
  address: Address
) extends BusinessPartnerIdentification

case class BusinessPartnerSoleProprietor(
  firstName: String,
  lastName: String,
  hasTradeName: Boolean,
  tradeName: Option[String],
  hasNino: Boolean,
  nino: Option[String],
  hasVat: Boolean,
  vat: Option[String],
  uniqueTaxpayerReference: Option[String],
  address: Address
) extends BusinessPartnerIdentification

case class BusinessPartnerPartnership(
  partnershipName: String,
  hasTradeName: Boolean,
  tradeName: Option[String],
  hasVat: Boolean,
  vat: Option[String],
  hasUniqueTaxpayerReference: Boolean,
  uniqueTaxpayerReference: Option[String],
  address: Address
) extends BusinessPartnerIdentification

case class BusinessPartnerLimitedLiabilityPartnership(
  limitedLiabilityPartnershipName: String,
  hasTradeName: Boolean,
  tradeName: Option[String],
  companyRegistrationNumber: String,
  hasVat: Boolean,
  vat: Option[String],
  uniqueTaxpayerReference: Option[String],
  address: Address
) extends BusinessPartnerIdentification

case class BusinessPartnerCorporateBody(
  companyName: String,
  hasTradeName: Boolean,
  tradeName: Option[String],
  companyRegistrationNumber: String,
  hasVat: Boolean,
  vat: Option[String],
  uniqueTaxpayerReference: Option[String],
  address: Address
) extends BusinessPartnerIdentification

case class BusinessPartnerUnincorporatedBody(
  unincorporatedBodyName: String,
  hasTradeName: Boolean,
  tradeName: Option[String],
  hasVat: Boolean,
  vat: Option[String],
  hasUniqueTaxpayerReference: Boolean,
  uniqueTaxpayerReference: Option[String],
  address: Address
) extends BusinessPartnerIdentification

object BusinessPartner {
  implicit val businessPartnerIndividualFormat = Json.format[BusinessPartnerIndividual]
  implicit val businessPartnerSoleProprietorFormat = Json.format[BusinessPartnerSoleProprietor]
  implicit val businessPartnerPartnershipFormat = Json.format[BusinessPartnerPartnership]
  implicit val businessPartnerLimitedLiabilityPartnershipFormat = Json.format[BusinessPartnerLimitedLiabilityPartnership]
  implicit val businessPartnerCorporateBodyFormat = Json.format[BusinessPartnerCorporateBody]
  implicit val businessPartnerUnincorporatedBodyFormat = Json.format[BusinessPartnerUnincorporatedBody]

  implicit val businessPartnerIdentificationWrites = new Writes[BusinessPartnerIdentification] {
    override def writes(o: BusinessPartnerIdentification): JsValue = {
      o match {
        case i: BusinessPartnerIndividual ⇒ Json toJson i
        case s: BusinessPartnerSoleProprietor    ⇒ Json toJson s
        case p: BusinessPartnerPartnership    ⇒ Json toJson p
        case l: BusinessPartnerLimitedLiabilityPartnership    ⇒ Json toJson l
        case c: BusinessPartnerCorporateBody    ⇒ Json toJson c
        case u: BusinessPartnerUnincorporatedBody    ⇒ Json toJson u
      }
    }
  }

  implicit val writes = Json.writes[BusinessPartner]
  implicit val reads = new Reads[BusinessPartner] {
    override def reads(value: JsValue): JsResult[BusinessPartner] = {
      value.validate[JsObject].flatMap { json ⇒
        (json \ "businessPartnerType") match {
          case JsDefined(JsString(t)) if t == Individual.toString ⇒
            (json \ "identification").validate[BusinessPartnerIndividual].map(BusinessPartner(Individual, _))
          case JsDefined(JsString(t)) if t == SoleProprietor.toString ⇒
            (json \ "identification").validate[BusinessPartnerSoleProprietor].map(BusinessPartner(SoleProprietor, _))
          case JsDefined(JsString(t)) if t == Partnership.toString ⇒
            (json \ "identification").validate[BusinessPartnerPartnership].map(BusinessPartner(Partnership, _))
          case JsDefined(JsString(t)) if t == LimitedLiabilityPartnership.toString ⇒
            (json \ "identification").validate[BusinessPartnerLimitedLiabilityPartnership].map(BusinessPartner(LimitedLiabilityPartnership, _))
          case JsDefined(JsString(t)) if t == CorporateBody.toString ⇒
            (json \ "identification").validate[BusinessPartnerCorporateBody].map(BusinessPartner(CorporateBody, _))
          case JsDefined(JsString(t)) if t == UnincorporatedBody.toString ⇒
            (json \ "identification").validate[BusinessPartnerUnincorporatedBody].map(BusinessPartner(UnincorporatedBody, _))
          case e: JsError => e
          case _ ⇒ JsError("unknown Business Partner type")
        }
      }

    }
  }

  implicit val businessPartnerFormat = Format(reads, writes)

}