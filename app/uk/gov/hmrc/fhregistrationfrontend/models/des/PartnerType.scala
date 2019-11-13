/*
 * Copyright 2019 HM Revenue & Customs
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

sealed trait PartnerType

case class IndividualPartnerType(name: Name, nino: Option[String]) extends PartnerType

case class SoleProprietorPartnerType(
  name: Name,
  nino: Option[String],
  identification: PartnerIdentification,
  tradingName: Option[String]
) extends PartnerType

case class LimitedLiabilityPartnershipType(
  names: CompanyName,
  identification: PartnerIdentification,
  incorporationDetails: IncorporationDetail
) extends PartnerType

case class PartnershipOrUnIncorporatedBodyPartnerType(
  names: CompanyName,
  identification: PartnerIdentification
) extends PartnerType

case class PartnerIdentification(
  vatRegistrationNumber: Option[String],
  uniqueTaxpayerReference: Option[String]
)

object PartnerType {
  implicit val partnerIdentificationFormat = Json.format[PartnerIdentification]

  implicit val individualPartnerTypeFormat = Json.format[IndividualPartnerType]
  implicit val soleProprietorPartnerTypeFormat = Json.format[SoleProprietorPartnerType]
  implicit val limitedLiabilityPartnershipTypeFormat = Json.format[LimitedLiabilityPartnershipType]
  implicit val partnershipOrUnIncorporatedBodyPartnerTypeFormat =
    Json.format[PartnershipOrUnIncorporatedBodyPartnerType]

//  val writes: Writes[PartnerType] = new Writes[PartnerType](
//
//  ) {
//    override def writes(o: PartnerType) = o match {
//      case p: SoleProprietorPartnerType ⇒
//    }
  val writes = new Writes[PartnerType] {
    override def writes(o: PartnerType) = o match {
      case obj: IndividualPartnerType ⇒ Json toJson obj
      case obj: SoleProprietorPartnerType ⇒ Json toJson obj
      case obj: LimitedLiabilityPartnershipType ⇒ Json toJson obj
      case obj: PartnershipOrUnIncorporatedBodyPartnerType ⇒ Json toJson obj
    }
  }

  val reads: Reads[PartnerType] = new Reads[PartnerType] {
    override def reads(json: JsValue) = json.validate[JsObject].flatMap { o ⇒
      if ((o \ "name").toOption.isDefined
          && (o \ "identification").toOption.isDefined) {
        soleProprietorPartnerTypeFormat reads json
      } else if ((o \ "name").toOption.isDefined) {
        individualPartnerTypeFormat reads json
      } else if ((o \ "incorporationDetails").toOption.isDefined) {
        limitedLiabilityPartnershipTypeFormat reads json
      } else {
        partnershipOrUnIncorporatedBodyPartnerTypeFormat reads json
      }
    }
  }

  implicit val format = Format(reads, writes)

}
