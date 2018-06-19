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

package uk.gov.hmrc.fhregistrationfrontend.actions

import java.time.LocalDate

import uk.gov.hmrc.fhregistrationfrontend.forms.models.ListWithTrackedChanges.Added
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.data.LtdLargeUk.verifiedEmail

object FormTestData {


  val contactPerson = ContactPerson(
    "Cosmin",
    "Marian",
    "Director",
    "11123",
    Some(verifiedEmail),
    false,
    Some(true),
    Some(Address(
      "Flat 1B",
      None,
      None,
      Some("Testtown"),
      "ZZ11 1ZZ",
      None
    )),
    None
  )

  val otherContactPerson = contactPerson copy (firstName = "John")

  val mainBusinessAddress = MainBusinessAddress(
    "Less than 3 years",
    Some(true),
    Some(Address(
      "Flat 1A",
      None,
      None,
      Some("Testtown"),
      "ZZ11 1ZZ",
      None
    )),
    Some(LocalDate.of(2015, 3, 20))
  )

  val nationalInsuranceNumber = NationalInsuranceNumber(true, Some("AA123111A"))
  val companyRegistrationNumber = CompanyRegistrationNumber("12345678")
  val dateOfIncorporation = DateOfIncorporation(LocalDate.of(2014, 3, 20))
  val tradingName = TradingName(true, Some("DodgyCo"))
  val vatNumber = VatNumber(true, Some("123456789"))
  val companyOfficers = ListWithTrackedChanges(
    List(CompanyOfficer(
      CompanyOfficerType.Individual,
      CompanyOfficerIndividual(
        "Cosmin",
        "M",
        true,
        Some("AA123123A"),
        None, None, None,
        "Company Secretary"
      )) → Added,
      CompanyOfficer(
        CompanyOfficerType.Individual,
        CompanyOfficerIndividual(
          "Vlad",
          "M",
          true,
          Some("AA123231"),
          None, None, None,
          "Director"
        )
      ) → Added,
      CompanyOfficer(
        CompanyOfficerType.Company,
        CompanyOfficerCompany(
          "Some Company",
          true,
          Some("123456789"),
          None,
          "Company Secretary"
        )
      ) → Added),
    List.empty,
    false)
  val businessStatus = BusinessStatus(true, Some(LocalDate.of(2018, 6, 30)))
  val importingActivities = ImportingActivities(true, Some(EoriNumber("1234123132", true)))
  val businessCustomers = BusinessCustomers("Over 100")
  val otherStoragePremises = OtherStoragePremises(
    true,
    ListWithTrackedChanges(
      List(
        StoragePremise(
          Address(
            "1 Some High Street",
            None,
            None,
            Some("Sometown"),
            "Z99 2YY",
            None),
          false) → Added,
        StoragePremise(
          Address(
            "25 Testing Close",
            None,
            None,
            Some("Othertown"),
            "Z9 3WW",
            None),
          true) → Added),
      List.empty,
      false))


  val anAddress = businessregistration.Address(
    line1 = "line1",
    line2 = "line2",
    line3 = None,
    line4 = None,
    postcode = Some("NE98 1ZZ"),
    country = "GB")

  val someBpr =
    businessregistration.BusinessRegistrationDetails(
      businessName = Some("Real Business Inc"),
      None,
      businessAddress = anAddress,
      safeId = Some("XE0001234567890"),
      utr = Some("1111111111")
    )
}
