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

package uk.gov.hmrc.fhregistrationfrontend.services.mapping

import javax.inject.Inject

import com.google.inject.ImplementedBy
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.{businessregistration, des}
import uk.gov.hmrc.fhregistrationfrontend.models.des.{AllOtherInformation, BusinessAddressForFHDDS, IsNewFulfilmentBusiness, SubscriptionDisplay}

@ImplementedBy(classOf[DesToFormImpl])
trait DesToForm {

  def businessRegistrationDetails(subscriptionDisplay: SubscriptionDisplay): BusinessRegistrationDetails
  def entityType(subscriptionDisplay: SubscriptionDisplay): BusinessType
  def limitedCompanyApplication(subscription: des.SubscriptionDisplay): LimitedCompanyApplication
  def soleProprietorApplication(subscription: des.SubscriptionDisplay): SoleProprietorApplication
  def partnershipApplication(subscription: des.SubscriptionDisplay): PartnershipApplication

}

@Inject
class DesToFormImpl extends DesToForm {
  val GBCountryCode = "GB"

  def entityType(subscriptionDisplay: SubscriptionDisplay) =
    EntityTypeMapping desToForm subscriptionDisplay.organizationType

  def limitedCompanyApplication(subscription: des.SubscriptionDisplay) = {
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
  }

  override def soleProprietorApplication(subscription: SubscriptionDisplay): SoleProprietorApplication = ???

  override def partnershipApplication(subscription: SubscriptionDisplay): PartnershipApplication = ???

  override def businessRegistrationDetails(subscriptionDisplay: SubscriptionDisplay): BusinessRegistrationDetails = {
    BusinessRegistrationDetails(
      None,
      businessAddress(subscriptionDisplay.businessAddressForFHDDS.currentAddress),
      None,
      subscriptionDisplay.businessDetail.nonProprietor.flatMap(_.identification.vatRegistrationNumber)
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

  def otherStoragePremises(allOtherInformation: AllOtherInformation) = OtherStoragePremises(
    allOtherInformation.numberOfpremises != "0",
    allOtherInformation.premises map premises
  )

  def premises(premise: des.Premises): StoragePremise = StoragePremise(
    address(premise.address),
    premise.thirdPartyPremises
  )

  def businessCustomers(allOtherInformation: AllOtherInformation) =
    BusinessCustomers(allOtherInformation.numberOfCustomers)

  def importingActivities(allOtherInformation: AllOtherInformation) =
    ImportingActivities(
      allOtherInformation.doesEORIExist,
      eoriNumber(allOtherInformation)
    )

  def eoriNumber(allOtherInformation: AllOtherInformation): Option[EoriNumber] =
    if (allOtherInformation.doesEORIExist)
      for {
        eori ← allOtherInformation.EORINumber
        importedOutsideEori ← eori.goodsImportedOutEORI
        number ← eori.EORINonVat orElse eori.EORIVat
      } yield {
        EoriNumber(number, importedOutsideEori)
      }
    else
      None

  def businessStatus(businessDetail: des.IsNewFulfilmentBusiness) =
    BusinessStatus(
      businessDetail.isNewFulfilmentBusiness,
      businessDetail.proposedStartDate
    )

  def companyOfficers(partners: Option[des.PartnerCorporateBody]): List[CompanyOfficer] =
    partners
    .flatMap(_.companyOfficials)
    .map( _ map companyOfficial)
    .get

  def companyOfficial(official: des.CompanyOfficial): CompanyOfficer = {
    official match {
      case i: des.IndividualAsOfficial ⇒
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
      case c: des.CompanyAsOfficial ⇒ CompanyOfficer(
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
  }

  def vatNumber(detail: des.BusinessDetail) = {
    detail
      .nonProprietor
      .flatMap(_.identification.vatRegistrationNumber)
      .fold(VatNumber(false, None)) {
        name ⇒ VatNumber(true, Some(name))
      }
  }

  def tradingName(detail: des.BusinessDetail) = {
    detail
      .nonProprietor
      .flatMap(_.tradingName)
      .fold(TradingName(false, None)) {
        name ⇒ TradingName(true, Some(name))
      }
  }


  def companyRegistrationNumber(businessDetails: des.BusinessDetail) =
    CompanyRegistrationNumber(
      businessDetails
        .limitedLiabilityPartnershipCorporateBody
        .flatMap(_.incorporationDetails.companyRegistrationNumber)
        .get)

  def dateOfIncorporation(businessDetails: des.BusinessDetail) =
    DateOfIncorporation(
      businessDetails
        .limitedLiabilityPartnershipCorporateBody
        .flatMap(_.incorporationDetails.dateOfIncorporation)
        .get
    )

  def contactPerson(cd: des.ContactDetail) = ContactPerson(
    cd.names.firstName,
    cd.names.lastName,
    roleInOrganization (cd.roleInOrganization.get),
    cd.commonDetails.telephone.get,
    cd.commonDetails.email.get,
    cd.usingSameContactAddress,
    ukOtherAddress(cd),
    otherUkContactAddress(cd),
    otherInternationalContactAddress(cd)
  )

  def roleInOrganization(roleInOrganization: des.RoleInOrganization): String = {
      roleInOrganization.otherRoleDescription get
  }

  def otherInternationalContactAddress(cd: des.ContactDetail): Option[InternationalAddress] =
    cd.address.filter(_.countryCode != GBCountryCode) map internationalAddress

  def ukOtherAddress(cd: des.ContactDetail): Option[Boolean] = {
    if (cd.usingSameContactAddress )
      None
    else {
      cd.address map (_.countryCode == GBCountryCode)
    }
  }

  def otherUkContactAddress(cd: des.ContactDetail): Option[Address] = {
    cd.address.filter(_.countryCode == GBCountryCode) map address
  }

  def internationalAddress(a: des.Address) =
    InternationalAddress(
      a.line1,
      a.line2,
      a.line3,
      a.line4.get,
      a.countryCode
    )

  def address(a: des.Address) =
    Address(
      a.line1,
      a.line2,
      a.line3,
      a.line4,
      a.postalCode.get,
      None
    )

  def mainBusinessAddress(mainAddress: des.BusinessAddressForFHDDS) = MainBusinessAddress(
    mainAddress.timeOperatedAtCurrentAddress,
    mainAddress.previousOperationalAddress map (_.anyPreviousOperatingAddress),
    mainAddress.previousOperationalAddress.flatMap(previousAddress),
    mainAddress.previousOperationalAddress.flatMap(previousAddressStartDate)
  )

  def previousAddressStartDate(pa:  des.PreviousOperationalAddress) = {
    for {
      prevAddressesDetail ← pa.previousOperationalAddressDetail
      prevAddressDetail ← prevAddressesDetail.headOption
    } yield {
      prevAddressDetail.previousAddressStartdate
    }
  }

  def previousAddress(pa:  des.PreviousOperationalAddress) = {
    for {
      prevAddressesDetail ← pa.previousOperationalAddressDetail
      prevAddressDetail ← prevAddressesDetail.headOption
    } yield {
      address(prevAddressDetail.previousAddress)
    }
  }


}
