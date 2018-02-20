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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Forms.{list, mapping, nonEmptyText}
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings._
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl.MappingsApi.{MappingOps, MappingWithKeyOps}
import uk.gov.hmrc.fhregistrationfrontend.forms.models._

object BusinessPartnersForm {

  val firstNameKey = "firstName"
  val lastNameKey = "lastName"
  val partnershipNameKey = "partnershipName"
  val limitedLiabilityPartnershipNameKey = "limitedLiabilityPartnershipName"
  val companyNameKey = "companyName"
  val unincorporatedBodyNameKey = "unincorporatedBodyName"

  val hasNationalInsuranceNumberKey = "hasNationalInsuranceNumber"
  val nationalInsuranceNumberKey = "nationalInsuranceNumber"

  val hasUniqueTaxpayerReferenceKey = "hasUniqueTaxpayerReference"
  val uniqueTaxpayerReferenceKey = "uniqueTaxpayerReference"

  val hasTradeNameKey = "hasTradeName"
  val tradeNameKey = "tradeName"

  val hasVatKey = "hasVat"
  val vatRegistrationKey = "vat"

  val companyRegistrationNumberKey = "companyRegistrationNumber"

  val addressKey = "partnerAddress"

  val businessPartnersTypeKey = "businessPartnersType"

  val businessPartnerIndividualKey = "businessPartnerIndividual"
  val businessPartnerSoleProprietorKey = "businessPartnerSoleProprietor"
  val businessPartnerPartnershipKey = "businessPartnerPartnership"
  val businessPartnerLimitedLiabilityPartnershipKey = "businessPartnerLimitedLiabilityPartnership"
  val businessPartnerCorporateBodyKey = "businessPartnerCorporateBody"
  val businessPartnerUnincorporatedBodyKey = "businessPartnerUnincorporatedBody"

  val businessPartnersKey = "businessPartners"

  val hasNinoMapping = hasNationalInsuranceNumberKey → yesOrNo
  val ninoMapping = nationalInsuranceNumberKey → (nino onlyWhen (hasNinoMapping is true))

  val hasTradeNameMapping = hasTradeNameKey → yesOrNo
  val tradeNameMapping = tradeNameKey → (tradingName onlyWhen (hasTradeNameMapping is true))

  val companyRegistrationNumberMapping = companyRegistrationNumberKey → companyRegistrationNumber

  val hasVatMapping = hasVatKey → yesOrNo
  val vatMapping = vatRegistrationKey → (vatRegistrationNumber onlyWhen (hasVatMapping is true))

  val hasUniqueTaxpayerReferenceMapping = hasUniqueTaxpayerReferenceKey → yesOrNo
  val uniqueTaxpayerReferenceMapping = uniqueTaxpayerReferenceKey → (uniqueTaxpayerReference onlyWhen (hasUniqueTaxpayerReferenceMapping is true))

  val addressMapping = addressKey → address

  val businessPartnersTypeMapping = businessPartnersTypeKey → enum(BusinessPartnersType)

  val businessPartnerIndividualMapping = mapping(
    firstNameKey → personName,
    lastNameKey → personName,
    hasNinoMapping,
    ninoMapping,
    addressMapping
  )(BusinessPartnerIndividual.apply)(BusinessPartnerIndividual.unapply)

  val businessPartnerSoleProprietorMapping = mapping(
    firstNameKey → personName,
    lastNameKey → personName,
    hasTradeNameMapping,
    tradeNameMapping,
    hasNinoMapping,
    ninoMapping,
    hasVatMapping,
    vatMapping,
    addressMapping
  )(BusinessPartnerSoleProprietor.apply)(BusinessPartnerSoleProprietor.unapply)

  val businessPartnerPartnershipMapping = mapping(
    partnershipNameKey → tradingName,
    hasTradeNameMapping,
    tradeNameMapping,
    hasVatMapping,
    vatMapping,
    hasUniqueTaxpayerReferenceMapping,
    uniqueTaxpayerReferenceMapping,
    addressMapping
  )(BusinessPartnerPartnership.apply)(BusinessPartnerPartnership.unapply)

  val businessPartnerLimitedLiabilityPartnershipMapping = mapping(
    limitedLiabilityPartnershipNameKey → tradingName,
    hasTradeNameMapping,
    tradeNameMapping,
    companyRegistrationNumberMapping,
    hasVatMapping,
    vatMapping,
    uniqueTaxpayerReferenceMapping,
    addressMapping
  )(BusinessPartnerLimitedLiabilityPartnership.apply)(BusinessPartnerLimitedLiabilityPartnership.unapply)

  val businessPartnerCorporateBodyMapping = mapping(
    companyNameKey → tradingName,
    hasTradeNameMapping,
    tradeNameMapping,
    companyRegistrationNumberMapping,
    hasVatMapping,
    vatMapping,
    uniqueTaxpayerReferenceMapping,
    addressMapping
  )(BusinessPartnerCorporateBody.apply)(BusinessPartnerCorporateBody.unapply)

  val businessPartnerUnincorporatedBodyMapping = mapping(
    unincorporatedBodyNameKey → tradingName,
    hasTradeNameMapping,
    tradeNameMapping,
    hasVatMapping,
    vatMapping,
    hasUniqueTaxpayerReferenceMapping,
    uniqueTaxpayerReferenceMapping,
    addressMapping
  )(BusinessPartnerUnincorporatedBody.apply)(BusinessPartnerUnincorporatedBody.unapply)

  val businessPartnerMapping: Mapping[BusinessPartner] = mapping(
    businessPartnersTypeMapping,
    businessPartnerIndividualKey → (businessPartnerIndividualMapping onlyWhen (businessPartnersTypeMapping is BusinessPartnersType.Individual)),
    businessPartnerSoleProprietorKey → (businessPartnerSoleProprietorMapping onlyWhen (businessPartnersTypeMapping is BusinessPartnersType.SoleProprietor)),
    businessPartnerPartnershipKey → (businessPartnerPartnershipMapping onlyWhen (businessPartnersTypeMapping is BusinessPartnersType.Partnership)),
    businessPartnerLimitedLiabilityPartnershipKey → (businessPartnerLimitedLiabilityPartnershipMapping onlyWhen (businessPartnersTypeMapping is BusinessPartnersType.LimitedLiabilityPartnership)),
    businessPartnerCorporateBodyKey → (businessPartnerCorporateBodyMapping onlyWhen (businessPartnersTypeMapping is BusinessPartnersType.CorporateBody)),
    businessPartnerUnincorporatedBodyKey → (businessPartnerUnincorporatedBodyMapping onlyWhen (businessPartnersTypeMapping is BusinessPartnersType.UnincorporatedBody))
  ) {
    case (businessPartnersType, individual, soleProprietor, partnership, limitedLiabilityPartnership, corporateBody, unincorporatedBody) ⇒
      BusinessPartner(
        businessPartnersType,
        individual.getOrElse(
          soleProprietor.getOrElse(
            partnership.getOrElse(
              limitedLiabilityPartnership.getOrElse(
                corporateBody.getOrElse(
                  unincorporatedBody.get)))))
      )
  } {
    case BusinessPartner(businessPartnerType, identification) ⇒ identification match {
      case i: BusinessPartnerIndividual ⇒ Some((businessPartnerType, Some(i), None, None, None, None, None))
      case s: BusinessPartnerSoleProprietor    ⇒ Some((businessPartnerType, None, Some(s), None, None, None, None))
      case p: BusinessPartnerPartnership    ⇒ Some((businessPartnerType, None, None, Some(p), None, None, None))
      case l: BusinessPartnerLimitedLiabilityPartnership    ⇒ Some((businessPartnerType, None, None, None, Some(l), None, None))
      case c: BusinessPartnerCorporateBody    ⇒ Some((businessPartnerType, None, None, None, None, Some(c), None))
      case u: BusinessPartnerUnincorporatedBody    ⇒ Some((businessPartnerType, None, None, None, None, None, Some(u)))
    }
  }

  val businessPartnerForm = Form(businessPartnerMapping)

  val businessPartnersForm = Form(
    mapping(
      businessPartnersKey → list(businessPartnerMapping)
    )(BusinessPartners.apply)(BusinessPartners.unapply)
  )
}
