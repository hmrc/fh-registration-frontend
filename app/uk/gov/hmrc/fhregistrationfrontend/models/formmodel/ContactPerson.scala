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

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, OFormat}

case class ContactPerson(
  firstName: String,
  lastName: String,
  jobTitle: String,
  telephone: String,
  email: String,
  sameContractAddress: Boolean,
  isUkAddress: Option[Boolean],
  contactAddress: Option[AddressModel]
) extends FormDetails

object ContactPerson {

  implicit val format: OFormat[ContactPerson] = Json.format[ContactPerson]

  def contactPersonForm = Form(
    mapping(
      "firstName_value" -> nonEmptyText,
      "lastName_value" -> nonEmptyText,
      "jobTitle_value" -> nonEmptyText,
      "telephone_value" -> nonEmptyText,
      "email_value" -> nonEmptyText,
      "sameContractAddress" -> of(CustomFormatters.requiredBooleanFormatter),
      "isUkAddress" -> optional(of(CustomFormatters.requiredBooleanFormatter)),
      "contactAddress" -> optional(AddressModel.addressMapping)
    )(ContactPerson.apply)(ContactPerson.unapply)
  )

}