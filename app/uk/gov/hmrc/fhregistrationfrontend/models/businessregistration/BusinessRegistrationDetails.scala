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

case class BusinessRegistrationDetails(
  businessName: Option[String],
  businessType: Option[String],
  businessAddress: Address,
  safeId: Option[String],
  utr: Option[String] = None,
  firstName: Option[String] = None,
  lastName: Option[String] = None,
  identification: Option[Identification] = None
)

object BusinessRegistrationDetails {

  def readWithFallback[T: Reads](primary: JsPath, fallback: JsPath): Reads[Option[T]] =
    new Reads[Option[T]] {
      override def reads(json: JsValue): JsResult[Option[T]] =
        primary.readNullable[T].reads(json) match {
          case JsSuccess(Some(value), _) => JsSuccess(Some(value))
          case _                         => fallback.readNullable[T].reads(json)
        }
    }

  implicit val businessRegistrationDetailsReads: Reads[BusinessRegistrationDetails] = (
    readWithFallback[String](__ \ "businessName", __ \ "business_name") and
      readWithFallback[String](__ \ "businessType", __ \ "business_type") and
      (__ \ "businessAddress").read[Address].orElse((__ \ "business_address").read[Address]) and
      readWithFallback[String](__ \ "safeId", __ \ "safe_id") and
      (__ \ "utr").readNullable[String] and
      readWithFallback[String](__ \ "firstName", __ \ "first_name") and
      readWithFallback[String](__ \ "lastName", __ \ "last_name") and
      (__ \ "identification").readNullable[Identification]
  )(BusinessRegistrationDetails.apply)

  implicit val businessRegistrationDetailsWrites: OWrites[BusinessRegistrationDetails] =
    new OWrites[BusinessRegistrationDetails] {
      def writes(b: BusinessRegistrationDetails): JsObject = Json.obj(
        "businessName"    -> b.businessName,
        "businessType"    -> b.businessType,
        "businessAddress" -> b.businessAddress,
        "safeId"          -> b.safeId,
        "utr"             -> b.utr,
        "firstName"       -> b.firstName,
        "lastName"        -> b.lastName,
        "identification"  -> b.identification
      )
    }

  implicit val formats: OFormat[BusinessRegistrationDetails] =
    OFormat(businessRegistrationDetailsReads, businessRegistrationDetailsWrites)
}
