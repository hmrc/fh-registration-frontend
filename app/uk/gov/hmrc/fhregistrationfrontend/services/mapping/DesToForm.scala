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

package uk.gov.hmrc.fhregistrationfrontend.services.mapping

import javax.inject.Inject

import scala.language.postfixOps
import com.google.inject.ImplementedBy
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration
import uk.gov.hmrc.fhregistrationfrontend.models.des
import uk.gov.hmrc.fhregistrationfrontend.forms.models
import uk.gov.hmrc.fhregistrationfrontend.models.des.SubscriptionDisplay

@ImplementedBy(classOf[DesToFormImpl])
trait DesToForm {

  def businessRegistrationDetails(subscriptionDisplay: des.SubscriptionDisplay): BusinessRegistrationDetails

  def contactEmail(subscriptionDisplay: des.SubscriptionDisplay): Option[String]

  def entityType(subscriptionDisplay: des.SubscriptionDisplay): BusinessType

  def limitedCompanyApplication(subscription: des.SubscriptionDisplay): LimitedCompanyApplication

  def soleProprietorApplication(subscription: des.SubscriptionDisplay): SoleProprietorApplication

  def partnershipApplication(subscription: des.SubscriptionDisplay): PartnershipApplication

  def loadApplicationFromDes(display: SubscriptionDisplay): BusinessEntityApplication

  def declaration(declaration: des.Declaration): Declaration
}

@Inject
class DesToFormImpl extends DesToForm {
  val GBCountryCode = "GB"

  def contactEmail(subscriptionDisplay: des.SubscriptionDisplay): Option[String] =
    subscriptionDisplay.contactDetail.commonDetails.email

  override def entityType(subscriptionDisplay: des.SubscriptionDisplay) =
    EntityTypeMapping desToForm subscriptionDisplay.organizationType

  override def loadApplicationFromDes(display: SubscriptionDisplay): BusinessEntityApplication =
    entityType(display) match {
      case BusinessType.CorporateBody =>
        limitedCompanyApplication(display)
      case BusinessType.SoleTrader =>
        soleProprietorApplication(display)
      case BusinessType.Partnership =>
        partnershipApplication(display)
    }

  override def limitedCompanyApplication(subscription: des.SubscriptionDisplay): LimitedCompanyApplication =
    LimitedCompanyApplication(
      mainBusinessAddress(subscription.businessAddressForFHDDS),
      contactPerson(subscription.contactDetail),
      companyRegistrationNumber(subscription.businessDetail),
      dateOfIncorporation(subscription.businessDetail),
      tradingName(subscription.businessDetail),
      vatNumber(subscription.businessDetail),
      companyOfficers(subscription.additionalBusinessInformation.partnerCorporateBody),
      businessStatus(subscription.FHbusinessDetail),
      importingActivities(subscription.additionalBusinessInformation.allOtherInformation),
      businessCustomers(subscription.additionalBusinessInformation.allOtherInformation),
      otherStoragePremises(subscription.additionalBusinessInformation.allOtherInformation)
    )

  override def soleProprietorApplication(subscription: des.SubscriptionDisplay): SoleProprietorApplication =
    SoleProprietorApplication(
      mainBusinessAddress(subscription.businessAddressForFHDDS),
      contactPerson(subscription.contactDetail),
      nationalInsuranceNumber(subscription.businessDetail),
      tradingNameForSoleProprietor(subscription.businessDetail),
      vatNumberForSoleProprietor(subscription.businessDetail),
      businessStatus(subscription.FHbusinessDetail),
      importingActivities(subscription.additionalBusinessInformation.allOtherInformation),
      businessCustomers(subscription.additionalBusinessInformation.allOtherInformation),
      otherStoragePremises(subscription.additionalBusinessInformation.allOtherInformation)
    )

  override def partnershipApplication(subscription: des.SubscriptionDisplay): PartnershipApplication =
    PartnershipApplication(
      mainBusinessAddress(subscription.businessAddressForFHDDS),
      contactPerson(subscription.contactDetail),
      tradingName(subscription.businessDetail),
      vatNumber(subscription.businessDetail),
      businessPartners(subscription.businessDetail.partnership),
      businessStatus(subscription.FHbusinessDetail),
      importingActivities(subscription.additionalBusinessInformation.allOtherInformation),
      businessCustomers(subscription.additionalBusinessInformation.allOtherInformation),
      otherStoragePremises(subscription.additionalBusinessInformation.allOtherInformation)
    )

  override def businessRegistrationDetails(
    subscriptionDisplay: des.SubscriptionDisplay): BusinessRegistrationDetails = {
    val utr: Option[String] = entityType(subscriptionDisplay) match {
      case BusinessType.CorporateBody =>
        subscriptionDisplay.businessDetail.nonProprietor.flatMap(_.identification.uniqueTaxpayerReference)
      case BusinessType.Partnership =>
        subscriptionDisplay.businessDetail.nonProprietor.flatMap(_.identification.uniqueTaxpayerReference)
      case BusinessType.SoleTrader =>
        subscriptionDisplay.businessDetail.soleProprietor.flatMap(_.identification.uniqueTaxpayerReference)
    }

    BusinessRegistrationDetails(
      None,
      None,
      businessAddress(subscriptionDisplay.businessAddressForFHDDS.currentAddress),
      None,
      utr
    )
  }

  def businessAddress(a: des.Address) =
    businessregistration.Address(
      a.line1,
      a.line2.get,
      a.line3,
      a.line4,
      a.postalCode,
      ""
    )

  def otherStoragePremises(allOtherInformation: des.AllOtherInformation) = OtherStoragePremises(
    allOtherInformation.numberOfpremises != "0",
    ListWithTrackedChanges fromValues (allOtherInformation.premises getOrElse List.empty map premises)
  )

  def premises(premise: des.Premises): StoragePremise = StoragePremise(
    address(premise.address),
    premise.thirdPartyPremises
  )

  def businessCustomers(allOtherInformation: des.AllOtherInformation) =
    BusinessCustomers(allOtherInformation.numberOfCustomers)

  def importingActivities(allOtherInformation: des.AllOtherInformation) =
    ImportingActivities(
      allOtherInformation.doesEORIExist,
      eoriNumber(allOtherInformation)
    )

  def eoriNumber(allOtherInformation: des.AllOtherInformation): Option[EoriNumber] =
    if (allOtherInformation.doesEORIExist)
      for {
        eori                <- allOtherInformation.EORINumber
        importedOutsideEori <- eori.goodsImportedOutEORI
        number              <- eori.EORINonVat orElse eori.EORIVat
      } yield {
        EoriNumber(number, importedOutsideEori)
      } else
      None

  def businessPartners(partnership: Option[des.Partnership]): ListWithTrackedChanges[BusinessPartner] = {
    val businessPartners = partnership.get.partnerDetails.map(
      businessPartner
    )
    ListWithTrackedChanges.fromValues(businessPartners)
  }

  def businessPartner(partner: des.PartnerDetail): BusinessPartner =
    partner.partnerTypeDetail match {
      case i: des.IndividualPartnerType =>
        BusinessPartner(
          BusinessPartnerType.Individual,
          BusinessPartnerIndividual(
            i.name.firstName,
            i.name.lastName,
            i.nino.nonEmpty,
            i.nino,
            address(partner.partnerAddress)
          )
        )
      case s: des.SoleProprietorPartnerType =>
        BusinessPartner(
          BusinessPartnerType.SoleProprietor,
          BusinessPartnerSoleProprietor(
            s.name.firstName,
            s.name.lastName,
            s.tradingName.nonEmpty,
            s.tradingName,
            s.nino.nonEmpty,
            s.nino,
            s.identification.vatRegistrationNumber.nonEmpty,
            s.identification.vatRegistrationNumber,
            s.identification.uniqueTaxpayerReference,
            address(partner.partnerAddress)
          )
        )
      case l: des.LimitedLiabilityPartnershipType =>
        if (partner.entityType == "Limited Liability Partnership") {
          BusinessPartner(
            BusinessPartnerType.LimitedLiabilityPartnership,
            BusinessPartnerLimitedLiabilityPartnership(
              l.names.companyName.get,
              l.names.tradingName.nonEmpty,
              l.names.tradingName,
              l.incorporationDetails.companyRegistrationNumber.get,
              l.identification.vatRegistrationNumber.nonEmpty,
              l.identification.vatRegistrationNumber,
              l.identification.uniqueTaxpayerReference,
              address(partner.partnerAddress)
            )
          )
        } else {
          BusinessPartner(
            BusinessPartnerType.CorporateBody,
            BusinessPartnerCorporateBody(
              l.names.companyName.get,
              l.names.tradingName.nonEmpty,
              l.names.tradingName,
              l.incorporationDetails.companyRegistrationNumber.get,
              l.identification.vatRegistrationNumber.nonEmpty,
              l.identification.vatRegistrationNumber,
              l.identification.uniqueTaxpayerReference,
              address(partner.partnerAddress)
            )
          )
        }
      case p: des.PartnershipOrUnIncorporatedBodyPartnerType =>
        if (partner.entityType == "Partnership") {
          BusinessPartner(
            BusinessPartnerType.Partnership,
            BusinessPartnerPartnership(
              p.names.companyName.get,
              p.names.tradingName.nonEmpty,
              p.names.tradingName,
              p.identification.vatRegistrationNumber.nonEmpty,
              p.identification.vatRegistrationNumber,
              p.identification.uniqueTaxpayerReference.nonEmpty,
              p.identification.uniqueTaxpayerReference,
              address(partner.partnerAddress)
            )
          )
        } else {
          BusinessPartner(
            BusinessPartnerType.UnincorporatedBody,
            BusinessPartnerUnincorporatedBody(
              p.names.companyName.get,
              p.names.tradingName.nonEmpty,
              p.names.tradingName,
              p.identification.vatRegistrationNumber.nonEmpty,
              p.identification.vatRegistrationNumber,
              p.identification.uniqueTaxpayerReference.nonEmpty,
              p.identification.uniqueTaxpayerReference,
              address(partner.partnerAddress)
            )
          )
        }

    }

  def businessStatus(businessDetail: des.IsNewFulfilmentBusiness) =
    BusinessStatus(
      businessDetail.isNewFulfilmentBusiness,
      businessDetail.proposedStartDate
    )

  def companyOfficers(partners: Option[des.PartnerCorporateBody]): ListWithTrackedChanges[CompanyOfficer] =
    ListWithTrackedChanges fromValues partners
      .flatMap(_.companyOfficials)
      .map(_ map companyOfficial)
      .get

  def companyOfficial(official: des.CompanyOfficial): CompanyOfficer =
    official match {
      case i: des.IndividualAsOfficial =>
        val hasNino = i.identification.nino.isDefined
        val hasPassportNumber = if (hasNino) None else Some(i.identification.passportNumber.isDefined)
        CompanyOfficer(
          CompanyOfficerType.Individual,
          CompanyOfficerIndividual(
            i.name.firstName,
            i.name.lastName,
            hasNino,
            i.identification.nino,
            hasPassportNumber,
            i.identification.passportNumber,
            i.identification.nationalIdNumber,
            i.role
          )
        )
      case c: des.CompanyAsOfficial =>
        CompanyOfficer(
          CompanyOfficerType.Company,
          CompanyOfficerCompany(
            c.name.companyName.get,
            c.identification.vatRegistrationNumber.isDefined,
            c.identification.vatRegistrationNumber,
            c.identification.companyRegistrationNumber,
            c.role
          )
        )

    }

  def vatNumber(detail: des.BusinessDetail) =
    detail.nonProprietor
      .flatMap(_.identification.vatRegistrationNumber)
      .fold(VatNumber(false, None)) { name =>
        VatNumber(true, Some(name))
      }

  def vatNumberForSoleProprietor(detail: des.BusinessDetail) =
    detail.soleProprietor
      .flatMap(_.identification.vatRegistrationNumber)
      .fold(VatNumber(false, None)) { name =>
        VatNumber(true, Some(name))
      }

  def tradingName(detail: des.BusinessDetail) =
    detail.nonProprietor
      .flatMap(_.tradingName)
      .fold(TradingName(false, None)) { name =>
        TradingName(true, Some(name))
      }

  def tradingNameForSoleProprietor(detail: des.BusinessDetail) =
    detail.soleProprietor
      .flatMap(_.tradingName)
      .fold(TradingName(false, None)) { name =>
        TradingName(true, Some(name))
      }

  def nationalInsuranceNumber(businessDetails: des.BusinessDetail): NationalInsuranceNumber =
    businessDetails.soleProprietor
      .map(
        id => {
          NationalInsuranceNumber(
            id.identification.nino.nonEmpty,
            id.identification.nino
          )
        }
      )
      .get

  def companyRegistrationNumber(businessDetails: des.BusinessDetail) =
    CompanyRegistrationNumber(
      businessDetails.limitedLiabilityPartnershipCorporateBody
        .flatMap(_.incorporationDetails.companyRegistrationNumber)
        .get,
      None
    )

  def dateOfIncorporation(businessDetails: des.BusinessDetail) =
    DateOfIncorporation(
      businessDetails.limitedLiabilityPartnershipCorporateBody
        .flatMap(_.incorporationDetails.dateOfIncorporation)
        .get
    )

  def contactPerson(cd: des.ContactDetail) = ContactPerson(
    cd.names.firstName,
    cd.names.lastName,
    roleInOrganization(cd.roleInOrganization.get),
    cd.commonDetails.telephone.get,
    cd.commonDetails.email,
    cd.usingSameContactAddress,
    ukOtherAddress(cd),
    otherUkContactAddress(cd),
    otherInternationalContactAddress(cd)
  )

  def roleInOrganization(roleInOrganization: des.RoleInOrganization): String =
    roleInOrganization.otherRoleDescription get

  def otherInternationalContactAddress(cd: des.ContactDetail): Option[InternationalAddress] =
    cd.address.filter(_.countryCode != GBCountryCode) map internationalAddress

  def ukOtherAddress(cd: des.ContactDetail): Option[Boolean] =
    if (cd.usingSameContactAddress)
      None
    else {
      cd.address map (_.countryCode == GBCountryCode)
    }

  def otherUkContactAddress(cd: des.ContactDetail): Option[models.Address] =
    cd.address.filter(_.countryCode == GBCountryCode) map address

  def internationalAddress(a: des.Address) =
    InternationalAddress(
      a.line1,
      a.line2,
      a.line3,
      a.line4.get,
      a.countryCode
    )

  def address(a: des.Address): models.Address =
    models.Address(
      a.line1,
      a.line2,
      a.line3,
      a.line4,
      a.postalCode.get,
      None,
      None
    )

  def mainBusinessAddress(mainAddress: des.BusinessAddressForFHDDS) = MainBusinessAddress(
    mainAddress.timeOperatedAtCurrentAddress,
    mainAddress.previousOperationalAddress map (_.anyPreviousOperatingAddress),
    mainAddress.previousOperationalAddress.flatMap(previousAddress),
    mainAddress.previousOperationalAddress.flatMap(previousAddressStartDate)
  )

  def previousAddressStartDate(pa: des.PreviousOperationalAddress) =
    for {
      prevAddressesDetail <- pa.previousOperationalAddressDetail
      prevAddressDetail   <- prevAddressesDetail.headOption
    } yield {
      prevAddressDetail.previousAddressStartdate
    }

  def previousAddress(pa: des.PreviousOperationalAddress) =
    for {
      prevAddressesDetail <- pa.previousOperationalAddressDetail
      prevAddressDetail   <- prevAddressesDetail.headOption
    } yield {
      address(prevAddressDetail.previousAddress)
    }

  override def declaration(declaration: des.Declaration): Declaration = Declaration(
    declaration.personName,
    declaration.personStatus,
    true,
    declaration.email,
    None
  )
}
