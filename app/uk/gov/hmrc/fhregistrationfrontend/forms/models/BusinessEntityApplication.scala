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

package uk.gov.hmrc.fhregistrationfrontend.forms.models

sealed trait BusinessEntityApplication {
  val vatNumber: VatNumber
  val importingActivities: ImportingActivities
  val businessCustomers: BusinessCustomers
  val otherStoragePremises: OtherStoragePremises
}

case class LimitedCompanyApplication(
  mainBusinessAddress: MainBusinessAddress,
  contactPerson: ContactPerson,
  companyRegistrationNumber: CompanyRegistrationNumber,
  dateOfIncorporation: DateOfIncorporation,
  tradingName: TradingName,
  vatNumber: VatNumber,
  companyOfficers: ListWithTrackedChanges[CompanyOfficer],
  businessStatus: BusinessStatus,
  importingActivities: ImportingActivities,
  businessCustomers: BusinessCustomers,
  otherStoragePremises: OtherStoragePremises
) extends BusinessEntityApplication

case class SoleProprietorApplication(
  mainBusinessAddress: MainBusinessAddress,
  contactPerson: ContactPerson,
  nationalInsuranceNumber: NationalInsuranceNumber,
  tradingName: TradingName,
  vatNumber: VatNumber,
  businessStatus: BusinessStatus,
  importingActivities: ImportingActivities,
  businessCustomers: BusinessCustomers,
  otherStoragePremises: OtherStoragePremises
) extends BusinessEntityApplication

case class PartnershipApplication(
  mainBusinessAddress: MainBusinessAddress,
  contactPerson: ContactPerson,
  tradingName: TradingName,
  vatNumber: VatNumber,
  businessPartners: ListWithTrackedChanges[BusinessPartner],
  businessStatus: BusinessStatus,
  importingActivities: ImportingActivities,
  businessCustomers: BusinessCustomers,
  otherStoragePremises: OtherStoragePremises
) extends BusinessEntityApplication
