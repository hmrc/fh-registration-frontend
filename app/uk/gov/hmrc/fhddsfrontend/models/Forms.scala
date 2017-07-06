/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.fhddsfrontend.models

import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.fhddsfrontend.models.CustomFormatters._

case class Confirm(value: Boolean) extends AnyVal

case class BusinessDetails(hasTradingName: Boolean, after2017: Boolean, howManyCustomer: Int)

object Forms {

  implicit val confirmFormat: OFormat[Confirm] = Json.format[Confirm]

  implicit val businessDetailsFormat: OFormat[BusinessDetails] = Json.format[BusinessDetails]

  def confirmForm(implicit messages: Messages) = Form(
    mapping(
      "confirm" -> of(requiredBooleanFormatter)
    )(Confirm.apply)(Confirm.unapply))

  def businessDetailsForm(implicit messages: Messages) = Form(
    mapping(
      "hasTradingName" -> of(requiredBooleanFormatter),
      "after2017" -> of(requiredBooleanFormatter),
      "howManyCustomer" -> of(requiredIntFormatter)
    )(BusinessDetails.apply)(BusinessDetails.unapply))
}

