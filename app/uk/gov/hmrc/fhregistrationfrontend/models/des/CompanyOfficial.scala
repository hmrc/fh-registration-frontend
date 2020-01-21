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

import play.api.libs.json._

sealed trait CompanyOfficial

case class IndividualAsOfficial(
  role: String,
  name: Name,
  identification: IndividualIdentification,
  modification: Option[Modification]
) extends CompanyOfficial

case class CompanyAsOfficial(
  role: String,
  name: CompanyName,
  identification: CompanyIdentification,
  modification: Option[Modification]
) extends CompanyOfficial

object IndividualAsOfficial {
  implicit val format = Json.format[IndividualAsOfficial]
}

object CompanyAsOfficial {
  implicit val format = Json.format[CompanyAsOfficial]
}

object CompanyOfficial {
  val reads: Reads[CompanyOfficial] = new Reads[CompanyOfficial] {
    override def reads(json: JsValue) = json.validate[JsObject].flatMap { o ⇒
      (o \ "name" \ "firstName") match {
        case JsDefined(_) ⇒ o.validate[IndividualAsOfficial]
        case _ ⇒ o.validate[CompanyAsOfficial]
      }
    }

  }

  val writes: Writes[CompanyOfficial] = new Writes[CompanyOfficial] {
    override def writes(o: CompanyOfficial) = o match {
      case individual: IndividualAsOfficial ⇒ IndividualAsOfficial.format.writes(individual)
      case company: CompanyAsOfficial ⇒ CompanyAsOfficial.format.writes(company)
    }
  }

  implicit val format = Format(reads, writes)

}
