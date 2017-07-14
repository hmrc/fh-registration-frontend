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

import play.api.libs.json._
import uk.gov.hmrc.domain.{SimpleObjectReads, SimpleObjectWrites, TaxIdentifier}

case class Utr(utr: String) extends TaxIdentifier {
  override def value: String = utr
}

object Utr {
  implicit val utrWrite: Writes[Utr] = new SimpleObjectWrites[Utr](_.value)
  implicit val utrRead: Reads[Utr] = new SimpleObjectReads[Utr]("utr", Utr.apply)
}
