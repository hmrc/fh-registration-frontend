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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Forms.{list, mapping}
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

  val hasNationalInsuranceNumberKey = "nationalInsuranceNumber_yesNo"
  val nationalInsuranceNumberKey = "nationalInsuranceNumber_value"
  val nationalInsuranceNumber = "nationalInsuranceNumber"

  val hasUniqueTaxpayerReferenceKey = "uniqueTaxpayerReference_yesNo"
  val uniqueTaxpayerReferenceKey = "uniqueTaxpayerReference_value"
  val uniqueTaxpayerReference = "uniqueTaxpayerReference"

  val hasTradeNameKey = "tradeName_yesNo"
  val tradeNameKey = "tradeName_value"
  val tradeName = "tradeName"

  val hasVatKey = "vat_yesNo"
  val vatRegistrationKey = "vat_value"
  val vat = "vat"

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

  val companyRegistrationNumberMapping = companyRegistrationNumberKey → companyRegistrationNumber

  val hasNinoMapping = hasNationalInsuranceNumberKey → yesOrNo
  val ninoMapping = nationalInsuranceNumberKey → (nino onlyWhen (hasNinoMapping is true withPrefix businessPartnerIndividualKey))

  val hasNinoSoleProprietorMapping = hasNationalInsuranceNumberKey → yesOrNo
  val ninoSoleProprietorMapping = nationalInsuranceNumberKey → (nino onlyWhen (hasNinoSoleProprietorMapping is true withPrefix businessPartnerSoleProprietorKey))

  val hasTradeNameMapping = hasTradeNameKey → yesOrNo
  val tradeNameMapping = tradeNameKey → (tradingName onlyWhen (hasTradeNameMapping is true withPrefix businessPartnerSoleProprietorKey))

  val hasTradeNamePartnershipMapping = hasTradeNameKey → yesOrNo
  val tradeNamePartnershipMapping = tradeNameKey → (tradingName onlyWhen (hasTradeNamePartnershipMapping is true withPrefix businessPartnerPartnershipKey))

  val hasTradeNameLimitedLiabilityMapping = hasTradeNameKey → yesOrNo
  val tradeNameLimitedLiabilityMapping = tradeNameKey → (tradingName onlyWhen (hasTradeNameLimitedLiabilityMapping is true withPrefix businessPartnerLimitedLiabilityPartnershipKey))

  val hasTradeNameCorporateBodyMapping = hasTradeNameKey → yesOrNo
  val tradeNameCorporateBodyMapping = tradeNameKey → (tradingName onlyWhen (hasTradeNameCorporateBodyMapping is true withPrefix businessPartnerCorporateBodyKey))

  val hasTradeNameUnincorporatedBodyMapping = hasTradeNameKey → yesOrNo
  val tradeNameUnincorporatedBodyMapping = tradeNameKey → (tradingName onlyWhen (hasTradeNameUnincorporatedBodyMapping is true withPrefix businessPartnerUnincorporatedBodyKey))

  val hasVatMapping = hasVatKey → yesOrNo
  val vatMapping = vatRegistrationKey → (vatRegistrationNumber onlyWhen (hasVatMapping is true withPrefix businessPartnerSoleProprietorKey))

  val hasVatPartnershipMapping = hasVatKey → yesOrNo
  val vatPartnershipMapping = vatRegistrationKey → (vatRegistrationNumber onlyWhen (hasVatPartnershipMapping is true withPrefix businessPartnerPartnershipKey))

  val hasVatUnincorporatedBodyMapping = hasVatKey → yesOrNo
  val vatUnincorporatedBodyMapping = vatRegistrationKey → (vatRegistrationNumber onlyWhen (hasVatUnincorporatedBodyMapping is true withPrefix businessPartnerUnincorporatedBodyKey))

  val hasVatSoleProprietorMapping = hasVatKey → yesOrNo
  val vatSoleProprietorMapping = vatRegistrationKey → (vatRegistrationNumber onlyWhen (hasVatSoleProprietorMapping is true withPrefix businessPartnerSoleProprietorKey))
  val uniqueTaxpayerReferenceSoleProprietorMapping = uniqueTaxpayerReferenceKey → (uniqueTaxpayerReferenceNumber onlyWhen (hasVatSoleProprietorMapping is false withPrefix businessPartnerSoleProprietorKey))

  val hasVatLimitedLiabilityMapping = hasVatKey → yesOrNo
  val vatLimitedLiabilityMapping = vatRegistrationKey → (vatRegistrationNumber onlyWhen (hasVatLimitedLiabilityMapping is true withPrefix businessPartnerLimitedLiabilityPartnershipKey))
  val uniqueTaxpayerReferenceLimitedLiabilityMapping = uniqueTaxpayerReferenceKey → (uniqueTaxpayerReferenceNumber onlyWhen (hasVatLimitedLiabilityMapping is false withPrefix businessPartnerLimitedLiabilityPartnershipKey))

  val hasVatCorporateBodyMapping = hasVatKey → yesOrNo
  val vatCorporateBodyMapping = vatRegistrationKey → (vatRegistrationNumber onlyWhen (hasVatCorporateBodyMapping is true withPrefix businessPartnerCorporateBodyKey))
  val uniqueTaxpayerReferenceCorporateBodyMapping = uniqueTaxpayerReferenceKey → (uniqueTaxpayerReferenceNumber onlyWhen (hasVatCorporateBodyMapping is false withPrefix businessPartnerCorporateBodyKey))

  val hasUniqueTaxpayerReferenceMapping = hasUniqueTaxpayerReferenceKey → yesOrNo
  val uniqueTaxpayerReferenceMapping = uniqueTaxpayerReferenceKey → (uniqueTaxpayerReferenceNumber onlyWhen (hasUniqueTaxpayerReferenceMapping is true withPrefix businessPartnerPartnershipKey))

  val hasUniqueTaxpayerReferenceUnincorporatedBodyMapping = hasUniqueTaxpayerReferenceKey → yesOrNo
  val uniqueTaxpayerReferenceUnincorporatedBodyMapping = uniqueTaxpayerReferenceKey → (uniqueTaxpayerReferenceNumber onlyWhen (hasUniqueTaxpayerReferenceUnincorporatedBodyMapping is true withPrefix businessPartnerUnincorporatedBodyKey))

  val addressMapping = addressKey → address

  val businessPartnersTypeMapping = businessPartnersTypeKey → enum(BusinessPartnerType)

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
    hasNinoSoleProprietorMapping,
    ninoSoleProprietorMapping,
    hasVatSoleProprietorMapping,
    vatSoleProprietorMapping,
    uniqueTaxpayerReferenceSoleProprietorMapping,
    addressMapping
  )(BusinessPartnerSoleProprietor.apply)(BusinessPartnerSoleProprietor.unapply)

  val businessPartnerPartnershipMapping = mapping(
    partnershipNameKey → tradingName,
    hasTradeNamePartnershipMapping,
    tradeNamePartnershipMapping,
    hasVatPartnershipMapping,
    vatPartnershipMapping,
    hasUniqueTaxpayerReferenceMapping,
    uniqueTaxpayerReferenceMapping,
    addressMapping
  )(BusinessPartnerPartnership.apply)(BusinessPartnerPartnership.unapply)

  val businessPartnerLimitedLiabilityPartnershipMapping = mapping(
    limitedLiabilityPartnershipNameKey → tradingName,
    hasTradeNameLimitedLiabilityMapping,
    tradeNameLimitedLiabilityMapping,
    companyRegistrationNumberMapping,
    hasVatLimitedLiabilityMapping,
    vatLimitedLiabilityMapping,
    uniqueTaxpayerReferenceLimitedLiabilityMapping,
    addressMapping
  )(BusinessPartnerLimitedLiabilityPartnership.apply)(BusinessPartnerLimitedLiabilityPartnership.unapply)

  val businessPartnerCorporateBodyMapping = mapping(
    companyNameKey → tradingName,
    hasTradeNameCorporateBodyMapping,
    tradeNameCorporateBodyMapping,
    companyRegistrationNumberMapping,
    hasVatCorporateBodyMapping,
    vatCorporateBodyMapping,
    uniqueTaxpayerReferenceCorporateBodyMapping,
    addressMapping
  )(BusinessPartnerCorporateBody.apply)(BusinessPartnerCorporateBody.unapply)

  val businessPartnerUnincorporatedBodyMapping = mapping(
    unincorporatedBodyNameKey → tradingName,
    hasTradeNameUnincorporatedBodyMapping,
    tradeNameUnincorporatedBodyMapping,
    hasVatUnincorporatedBodyMapping,
    vatUnincorporatedBodyMapping,
    hasUniqueTaxpayerReferenceUnincorporatedBodyMapping,
    uniqueTaxpayerReferenceUnincorporatedBodyMapping,
    addressMapping
  )(BusinessPartnerUnincorporatedBody.apply)(BusinessPartnerUnincorporatedBody.unapply)

  val businessPartnerMapping: Mapping[BusinessPartner] = mapping(
    businessPartnersTypeMapping,
    businessPartnerIndividualKey → (businessPartnerIndividualMapping onlyWhen (businessPartnersTypeMapping is BusinessPartnerType.Individual)),
    businessPartnerSoleProprietorKey → (businessPartnerSoleProprietorMapping onlyWhen (businessPartnersTypeMapping is BusinessPartnerType.SoleProprietor)),
    businessPartnerPartnershipKey → (businessPartnerPartnershipMapping onlyWhen (businessPartnersTypeMapping is BusinessPartnerType.Partnership)),
    businessPartnerLimitedLiabilityPartnershipKey → (businessPartnerLimitedLiabilityPartnershipMapping onlyWhen (businessPartnersTypeMapping is BusinessPartnerType.LimitedLiabilityPartnership)),
    businessPartnerCorporateBodyKey → (businessPartnerCorporateBodyMapping onlyWhen (businessPartnersTypeMapping is BusinessPartnerType.CorporateBody)),
    businessPartnerUnincorporatedBodyKey → (businessPartnerUnincorporatedBodyMapping onlyWhen (businessPartnersTypeMapping is BusinessPartnerType.UnincorporatedBody))
  ) {
    case (
        businessPartnersType,
        individual,
        soleProprietor,
        partnership,
        limitedLiabilityPartnership,
        corporateBody,
        unincorporatedBody) ⇒
      BusinessPartner(
        businessPartnersType,
        individual.getOrElse(
          soleProprietor.getOrElse(partnership.getOrElse(
            limitedLiabilityPartnership.getOrElse(corporateBody.getOrElse(unincorporatedBody.get)))))
      )
  } {
    case BusinessPartner(businessPartnerType, identification) ⇒
      identification match {
        case i: BusinessPartnerIndividual ⇒ Some((businessPartnerType, Some(i), None, None, None, None, None))
        case s: BusinessPartnerSoleProprietor ⇒ Some((businessPartnerType, None, Some(s), None, None, None, None))
        case p: BusinessPartnerPartnership ⇒ Some((businessPartnerType, None, None, Some(p), None, None, None))
        case l: BusinessPartnerLimitedLiabilityPartnership ⇒
          Some((businessPartnerType, None, None, None, Some(l), None, None))
        case c: BusinessPartnerCorporateBody ⇒ Some((businessPartnerType, None, None, None, None, Some(c), None))
        case u: BusinessPartnerUnincorporatedBody ⇒ Some((businessPartnerType, None, None, None, None, None, Some(u)))
      }
  }

  val businessPartnerForm = Form(businessPartnerMapping)

  val businessPartnersForm = Form(
    mapping(
      businessPartnersKey → list(businessPartnerMapping)
    )(BusinessPartners.apply)(BusinessPartners.unapply)
  )
}
