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

import play.api.libs.json.Json

case class MainBusinessAddress (
  timeAtCurrentAddress: String,
  hasPreviousAddress: Option[Boolean],
  previousAddress: Option[Address]
)

object MainBusinessAddress {

  implicit val format = Json.format[MainBusinessAddress]

  val TimeAtCurrentAddressOptions = Seq(
    "Less than 3 years",
    "3 to 5 years",
    "5 to 10 years",
    "10 or more years"
  )
}

