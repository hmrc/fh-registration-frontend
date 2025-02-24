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

package uk.gov.hmrc.fhregistrationfrontend.models.businessregistration

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.functional._

case class Identification(idNumber: String, issuingInstitution: String, issuingCountryCode: String)

object Identification {
  implicit val identificationReads: Reads[Identification] = (
    (__ \ "idNumber").read[String].orElse((__ \ "id_number").read[String]) and
      (__ \ "issuingInstitution").read[String].orElse((__ \ "issuing_institution").read[String]) and
      (__ \ "issuingCountryCode").read[String].orElse((__ \ "issuing_country_code").read[String])
  )(Identification.apply _)

  implicit val identificationWrites: OWrites[Identification] = new OWrites[Identification] {
    def writes(identification: Identification): JsObject = Json.obj(
      "idNumber"           -> identification.idNumber,
      "issuingInstitution" -> identification.issuingInstitution,
      "issuingCountryCode" -> identification.issuingCountryCode
    )
  }

  implicit val identificationFormat: OFormat[Identification] =
    OFormat(identificationReads, identificationWrites)
}
