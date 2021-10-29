/*
 * Copyright 2021 HM Revenue & Customs
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

import com.github.tototoshi.play.json.JsonNaming
import play.api.libs.json.Json

case class Address(
  line1: String,
  line2: String,
  line3: Option[String] = None,
  line4: Option[String] = None,
  postcode: Option[String] = None,
  country: String) {

  override def toString = {
    val line3display = line3.map(line3 => s"$line3, ").getOrElse("")
    val line4display = line4.map(line4 => s"$line4, ").getOrElse("")
    val postcodeDisplay = postcode.map(postcode1 => s"$postcode1, ").getOrElse("")
    s"$line1, $line2, $line3display$line4display$postcodeDisplay$country"
  }
}

object Address {
  implicit val formats = JsonNaming snakecase Json.format[Address]
}
