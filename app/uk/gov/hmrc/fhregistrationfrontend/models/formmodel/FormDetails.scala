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

package uk.gov.hmrc.fhregistrationfrontend.models.formmodel

import play.api.libs.json._

trait FormDetails

object FormDetails {

  implicit val mainBusinessAddressFormat: OFormat[MainBusinessAddress] = Json.format[MainBusinessAddress]
  implicit val contactPersonFormat: OFormat[ContactPerson] = Json.format[ContactPerson]

  val writes = new Writes[FormDetails] {
    override def writes(o: FormDetails) = o match {
      case obj: MainBusinessAddress ⇒ Json toJson obj
      case obj: ContactPerson ⇒ Json toJson obj
    }
  }

  val reads: Reads[FormDetails] = new Reads[FormDetails] {
    override def reads(json: JsValue) = json.validate[JsObject].flatMap { o ⇒
      if ((o \ "firstName").toOption.isDefined) {
        contactPersonFormat reads json
      } else {
        mainBusinessAddressFormat reads json
      }
    }
  }

  implicit val format = Format(reads, writes)
}