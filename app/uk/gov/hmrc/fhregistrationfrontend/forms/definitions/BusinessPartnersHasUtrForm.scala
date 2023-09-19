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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{uniqueTaxpayerReferenceNumber, yesOrNo}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl.MappingsApi.{MappingOps, MappingWithKeyOps}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.hasUniqueTaxpayerReference

object BusinessPartnersHasUtrForm {

  val hasBusinessPartnerUtrKey = "uniqueTaxpayerReference_yesNo"
  val businessPartnerUtrKey = "uniqueTaxpayerReference_value"

  private val hasBusinessPartnerUtrMapping = hasBusinessPartnerUtrKey -> yesOrNo
  private val businessPartnerUtrMapping = businessPartnerUtrKey ->
    (uniqueTaxpayerReferenceNumber onlyWhen (hasBusinessPartnerUtrMapping is true))

  val businessPartnerUtrForm = Form(
    mapping(
      hasBusinessPartnerUtrMapping,
      businessPartnerUtrMapping
    )(hasUniqueTaxpayerReference.apply)(hasUniqueTaxpayerReference.unapply)
  )
}
