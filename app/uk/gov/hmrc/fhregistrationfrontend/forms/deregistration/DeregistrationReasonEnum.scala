/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.forms.deregistration
import play.api.libs.json.{Format, Reads, Writes}

object DeregistrationReasonEnum extends Enumeration {
  type DeregistrationReasonEnum = Value

  val NoLongerNeeded = Value("CEASES_REGISTERABLE_SCHEME")
  val StoppedTrading = Value("CEASES_TRADE_FULFILMENT_BUSINESS")
  val ChangedLegalEntity = Value("CHANGE_LEGAL_ENTITY")
  val Other = Value("Others")

  implicit val format = Format(
    Reads.enumNameReads(DeregistrationReasonEnum),
    Writes.enumNameWrites[this.type]
  )

}
