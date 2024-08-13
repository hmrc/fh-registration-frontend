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

import play.api.libs.json.{Json, OFormat}

case class AlternativeEmail(
  email: String
)

case class Declaration(
  fullName: String,
  jobTitle: String,
  usingDefaultEmail: Boolean,
  defaultEmail: Option[String],
  alternativeEmail: Option[AlternativeEmail]
) {

  def email: String =
    if (usingDefaultEmail)
      defaultEmail.get
    else
      alternativeEmail.map(_.email).get
}

object Declaration {
  implicit val alternativeEmailFormat: OFormat[AlternativeEmail] = Json.format[AlternativeEmail]
  implicit val declarationFormat: OFormat[Declaration] = Json.format[Declaration]
}
