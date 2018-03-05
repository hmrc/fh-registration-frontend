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

import java.time.LocalDate

import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, _}
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.{businessregistration, des}
import uk.gov.hmrc.fhregistrationfrontend.models.des.Modification

trait FormToDes {
  def limitedCompanySubmission(bpr: BusinessRegistrationDetails, application: LimitedCompanyApplication, d: Declaration): des.Subscription
  def soleProprietorCompanySubmission(bpr: BusinessRegistrationDetails, application: SoleProprietorApplication, d: Declaration): des.Subscription
  def partnership(bpr: BusinessRegistrationDetails, application: PartnershipApplication, d: Declaration): des.Subscription

  def withModificationFlags(withModificationFlags: Boolean = false, changeDate: Option[LocalDate]): FormToDes
}

case class FormToDesImpl(withModificationFlags: Boolean = false, changeDate: Option[LocalDate] = None) extends FormToDes {

  def withModificationFlags(withModificationFlags: Boolean = false, changeDate: Option[LocalDate]): FormToDes = this copy (
    withModificationFlags = true,
    changeDate = changeDate
  )

  def soleProprietorCompanySubmission(bpr: BusinessRegistrationDetails, application: SoleProprietorApplication, d: Declaration): des.Subscription = ???
  def partnership(bpr: BusinessRegistrationDetails, application: PartnershipApplication, d: Declaration): des.Subscription = ???

  override def limitedCompanySubmission(bpr: BusinessRegistrationDetails, application: LimitedCompanyApplication, d: Declaration): des.Subscription =
    des.Subscription(
      EntityTypeMapping formToDes BusinessType.CorporateBody,
      isNewFulfilmentBusiness(application.businessStatus),
      None,
      additionalBusinessInformation(application),
      businessDetail(application, bpr),
      businessAddress(bpr, application.mainBusinessAddress),
      contactDetail(bpr, application.contactPerson),
      declaration(d)
    )

  def declaration(d: Declaration): des.Declaration = des.Declaration(
    d.fullName,
    d.jobTitle,
    Some(d.email),
    true
  )

  def contactDetail(bpr: BusinessRegistrationDetails, contact: ContactPerson) =
    des.ContactDetail(
      None,
      des.Name(contact.firstName, None, contact.lastName),
      contact.usingSameContactAddress,
      contactDetailAddress(bpr, contact),
      des.CommonDetails(Some(contact.telephone), None, Some(contact.emailAddress)),
      Some(des.RoleInOrganization otherRole contact.jobTitle) //TODO: job title is the role?
    )

  def internationalAddress(a: InternationalAddress) =
    des.Address(
      a.addressLine1,
      a.addressLine2,
      a.addressLine3,
      Some(a.addressLine4),
      None,
      a.countryCode
    )

  def contactDetailAddress(bpr: BusinessRegistrationDetails, contact: ContactPerson): Option[des.Address] = {
    //TODO move this logic to ContactPerson?
    contact.otherUkContactAddress.map (address(_))
      .orElse(contact.otherInternationalContactAddress.map(internationalAddress(_)))
  }

  def businessAddress(bpr: BusinessRegistrationDetails, mainAddress: MainBusinessAddress): des.BusinessAddressForFHDDS =
    des.BusinessAddressForFHDDS(
      currentAddress(bpr.businessAddress),
      des.CommonDetails(None, None, None),
      mainAddress.timeAtCurrentAddress,
      previousOperationalAddress(mainAddress)
    )


  def currentAddress(ba: businessregistration.Address) =
    des.Address(
      ba.line1,
      Some(ba.line2),
      ba.line3,
      ba.line4,
      ba.postcode,
      "GB"//TODO can main business address be in a different country?
    )


  def previousOperationalAddress(mainBusinessAddress: MainBusinessAddress) =
    for {
      address ← mainBusinessAddress.previousAddress
      start ← mainBusinessAddress.previousAddressStartdate
    } yield {
      des.PreviousOperationalAddress(
        true,
        Some(List(previousOperationalAddressDetail(start, address))))
    }


  def previousOperationalAddressDetail(start: LocalDate, a: Address) =
    des.PreviousOperationalAddressDetail(
      address(a),
      start
    )

  def businessDetail(application: LimitedCompanyApplication, bpr: BusinessRegistrationDetails) = {
    import application._
    des.BusinessDetail(
      None,
      Some(nonProprietor(tradingName, vatNumber, bpr)),
      Some(llpOrCorporate(companyRegistrationNumber, dateOfIncorporation)),
      None
    )
  }



  //  def soleProprietor(tradingName: TradingName) =
//    SoleProprietor(
//      tradingName.value,
//      SoleProprietorIdentification(
//
//      )
//    )

  def llpOrCorporate(crn: CompanyRegistrationNumber, dateOfIncorporation: DateOfIncorporation) =
    des.LimitedLiabilityPartnershipCorporateBody(
      None,
      des.IncorporationDetails(
        Some(crn.crn),
        Some(dateOfIncorporation.dateOfIncorporation)
      )
    )

  def nonProprietor(tradingName: TradingName, vatNumber: VatNumber, bpr: BusinessRegistrationDetails) =
    des.NonProprietor(
      tradingName.value,
      des.NonProprietorIdentification(
        vatNumber.value,
        bpr.utr
      )
    )

  def additionalBusinessInformation(application: LimitedCompanyApplication) =
    des.AdditionalBusinessInformationwithType(
      Some(partnerCorporateBody(application.companyOfficers)),
      allOtherInformation(application)
    )


  def allOtherInformation(application: LimitedCompanyApplication) = {
    import application._
    val desPremises = if (otherStoragePremises.hasValue) repeatedValue(premise, otherStoragePremises.value) else List.empty

    des.AllOtherInformation(
      businessCustomers.numberOfCustomers,
      importingActivities.hasEori,
      importingActivities.eoriNumber map eoriNumberType(vatNumber.hasValue),
      desPremises.size.toString,
      desPremises

    )
  }


   val premise: (StoragePremise, Option[des.Modification]) ⇒ des.Premises =  { (p, modification) ⇒
    des.Premises(
      address(p.address),
      p.isThirdParty,
      modification
    )
   }


  def address(address: Address): des.Address =
    des.Address(
      address.addressLine1,
      address.addressLine2,
      address.addressLine3,
      address.addressLine4,
      Some(address.postcode),
      address.countryCode.getOrElse("GB")
    )


  def eoriNumberType(hasVat: Boolean)(eoriNumber: EoriNumber) =
    des.EORINumberType(
      if (hasVat) Some(eoriNumber.eoriNumber) else None,
      if (!hasVat) Some(eoriNumber.eoriNumber) else None,
      Some(eoriNumber.goodsImportedOutsideEori)
    )

  //limited company
  def isNewFulfilmentBusiness(bs: BusinessStatus)= des.IsNewFulfilmentBusiness (
    bs.isNewFulfilmentBusiness,
    bs.proposedStartDate
  )

  def partnerCorporateBody(officers: ListWithTrackedChanges[CompanyOfficer]) = {
    val desOfficials = repeatedValue(companyOfficial, officers)
    des.PartnerCorporateBody(
      desOfficials.size.toString,
      Some(desOfficials)

    )
  }

  def repeatedValue[T, D](t: (T, Option[Modification]) ⇒ D, list: ListWithTrackedChanges[T]): List[D] = {
    val values = list.valuesWithStatus map {
      case (v, changeStatus) ⇒ t(v, modification(changeStatus))
    }

    if (withModificationFlags)
      values ++ (list.deleted.map(v ⇒ t(v, Some(des.Modification("Removed", changeDate)))))
    else
      values
  }


  def modification(changeStatus: ListWithTrackedChanges.Status) = {
    if (withModificationFlags)
      changeStatus match {
        case ListWithTrackedChanges.NoChange ⇒ None
        case ListWithTrackedChanges.Added    ⇒ Some(Modification("Added", changeDate))
        case ListWithTrackedChanges.Updated  ⇒ Some(Modification("Updated", changeDate))
      }
    else
      None
  }

  val companyOfficial: (CompanyOfficer, Option[Modification]) ⇒ des.CompanyOfficial = { (officer, modification) ⇒
    officer.identification match {
      case i: CompanyOfficerIndividual ⇒ individualAsOfficial(i, modification)
      case c: CompanyOfficerCompany    ⇒ companyAsOfficial(c, modification)
    }
  }

  def companyAsOfficial(c: CompanyOfficerCompany, modification: Option[des.Modification]) = {
    import c._
    des.CompanyAsOfficial(
      role,
      des.CompanyName(Some(companyName), None),
      des.CompanyIdentification(c.vat, None, c.crn),
      modification
    )
  }

  def individualAsOfficial(i: CompanyOfficerIndividual, modification: Option[Modification]) = {
    import i._
    des.IndividualAsOfficial(
      role,
      des.Name(firstName, None, lastName),
      des.IndividualIdentification(
        passport,
        nationalId,
        nino
      ),
      modification
    )
  }

}
