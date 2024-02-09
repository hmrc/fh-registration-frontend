/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.models.businessPartners

import play.api.i18n.Messages
import play.api.libs.json.{Format, Reads, Writes}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import java.beans.Introspector

object BusinessPartnerType extends Enumeration {
  type BusinessPartnerTypes = Value

  val Individual, SoleProprietor, Partnership, LimitedLiabilityPartnership, CorporateBody, UnincorporatedBody = Value

  val partnerTypes: Seq[BusinessPartnerType.Value] =
    Seq(Individual, SoleProprietor, Partnership, LimitedLiabilityPartnership, CorporateBody, UnincorporatedBody)

  def options(implicit messages: Messages): Seq[RadioItem] = partnerTypes.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"fh.business_partners.entity_type.${Introspector.decapitalize(value.toString)}.label")),
        value = Some(value.toString),
        id = Some(s"value_$index")
      )
  }

  implicit val format: Format[BusinessPartnerType.Value] =
    Format[BusinessPartnerType.Value](Reads.enumNameReads(BusinessPartnerType), Writes.enumNameWrites)
}
