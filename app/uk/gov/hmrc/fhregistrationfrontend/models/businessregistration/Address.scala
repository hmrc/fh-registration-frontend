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

case class Address(
  line1: String,
  line2: String,
  line3: Option[String] = None,
  line4: Option[String] = None,
  postcode: Option[String] = None,
  country: String
) {

  override def toString = {
    val line3display = line3.map(line3 => s"$line3, ").getOrElse("")
    val line4display = line4.map(line4 => s"$line4, ").getOrElse("")
    val postcodeDisplay = postcode.map(postcode1 => s"$postcode1, ").getOrElse("")
    s"$line1, $line2, $line3display$line4display$postcodeDisplay$country"
  }
}

object Address {

  def readWithFallback[T: Reads](primary: JsPath, fallback: JsPath): Reads[Option[T]] = new Reads[Option[T]] {
    override def reads(json: JsValue): JsResult[Option[T]] =
      primary.readNullable[T].reads(json) match {
        case JsSuccess(Some(value), _) => JsSuccess(Some(value))
        case _                         => fallback.readNullable[T].reads(json)
      }
  }
  implicit val addressReads: Reads[Address] = (
    (__ \ "line1").read[String].orElse((__ \ "line_1").read[String]) and
      (__ \ "line2").read[String].orElse((__ \ "line_2").read[String]) and
      readWithFallback[String](__ \ "line3", __ \ "line_3") and
      readWithFallback[String](__ \ "line4", __ \ "line_4") and
      (__ \ "postcode").readNullable[String] and
      (__ \ "country").read[String]
  )(Address.apply _)

  implicit val addressWrites: OWrites[Address] = new OWrites[Address] {
    def writes(addr: Address): JsObject = Json.obj(
      "line1"    -> addr.line1,
      "line2"    -> addr.line2,
      "line3"    -> addr.line3,
      "line4"    -> addr.line4,
      "postcode" -> addr.postcode,
      "country"  -> addr.country
    )
  }

  implicit val addressFormat: OFormat[Address] = OFormat(addressReads, addressWrites)
}
