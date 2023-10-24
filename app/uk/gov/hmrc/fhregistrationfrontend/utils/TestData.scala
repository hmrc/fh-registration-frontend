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

package uk.gov.hmrc.fhregistrationfrontend.utils

import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, BusinessPartnerIndividual, BusinessPartnerLimitedLiabilityPartnership, BusinessPartnerUnincorporatedBody}

object TestData {

  val address: Address = Address(
    addressLine1 = "1 Romford Road",
    addressLine2 = Some("Wellington"),
    addressLine3 = Some("Telford"),
    addressLine4 = None,
    postcode = "TF1 4ER",
    countryCode = None,
    lookupId = None
  )

  val individualSummaryModel =
    BusinessPartnerIndividual("first name", "last name", hasNino = true, Some("QQ123456C"), address)

  val llpSummaryModel = BusinessPartnerLimitedLiabilityPartnership(
    "llp trading name",
    hasTradeName = false,
    Some("trade partner name"),
    "01234567",
    hasVat = false,
    vat = Some("123456789"),
    uniqueTaxpayerReference = Some("1234567890"),
    address
  )

  val unincoporateBodyModel = BusinessPartnerUnincorporatedBody(
    "unincorporated name",
    hasTradeName = false,
    Some("unincorporated trade name"),
    hasVat = false,
    vat = Some("123456789"),
    hasUniqueTaxpayerReference = false,
    uniqueTaxpayerReference = Some("1234567890"),
    address
  )
}
