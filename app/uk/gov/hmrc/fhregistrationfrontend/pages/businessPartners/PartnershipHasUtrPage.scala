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

package uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners

import pages.QuestionPage
import play.api.libs.json.JsPath
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.HasUniqueTaxpayerReference

case class PartnershipHasUtrPage(index: Int) extends QuestionPage[HasUniqueTaxpayerReference] {
  override def path: JsPath = JsPath \ "businessPartners" \ index.toString \ toString

  override def toString: String = "partnershipHasUtr"
}
