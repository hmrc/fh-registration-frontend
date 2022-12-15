/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.services.mapping

import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType.BusinessType

object EntityTypeMapping {

  def desToForm(organizationType: String) = organizationType match {
    case "Corporate Body" => BusinessType.CorporateBody
    case "Partnership" => BusinessType.Partnership
    case "Sole Proprietor" => BusinessType.SoleTrader
    case _ => throw new ReverseMappingException("unknwon entity type")
  }

  def formToDes(businessType: BusinessType) = businessType match {
    case BusinessType.CorporateBody => "Corporate Body"
    case BusinessType.Partnership => "Partnership"
    case BusinessType.SoleTrader => "Sole Proprietor"
  }

}
