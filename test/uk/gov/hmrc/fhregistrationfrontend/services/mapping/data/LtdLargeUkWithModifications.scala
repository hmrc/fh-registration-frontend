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

package uk.gov.hmrc.fhregistrationfrontend.services.mapping.data

import java.time.LocalDate

import uk.gov.hmrc.fhregistrationfrontend.forms.models._

object LtdLargeUkWithModifications {
  val application = LimitedCompanyApplication(
    MainBusinessAddress(
      "Less than 3 years",
      Some(true),
      Some(Address("Flat 1A", None, None, Some("Testtown"), "ZZ11 1ZZ", None, None)),
      Some(LocalDate.of(2015, 3, 20))
    ),
    ContactPerson(
      "Cosmin",
      "Marian",
      "Director",
      "11123",
      None,
      false,
      Some(true),
      Some(Address("Flat 1B", None, None, Some("Testtown"), "ZZ11 1ZZ", None, None)),
      None
    ),
    CompanyRegistrationNumber("12345678", "12345678"),
    DateOfIncorporation(LocalDate.of(2014, 3, 20)),
    TradingName(true, Some("DodgyCo")),
    VatNumber(true, Some("123456789")),
    ListWithTrackedChanges(
      List(
        CompanyOfficer(
          CompanyOfficerType.Individual,
          CompanyOfficerIndividual(
            "Cosmin",
            "M",
            true,
            Some("AA123123A"),
            None,
            None,
            None,
            "Company Secretary"
          )) -> ListWithTrackedChanges.Added,
        CompanyOfficer(
          CompanyOfficerType.Individual,
          CompanyOfficerIndividual(
            "Vlad",
            "M",
            true,
            Some("AA123231"),
            None,
            None,
            None,
            "Director"
          )) -> ListWithTrackedChanges.NoChange,
        CompanyOfficer(
          CompanyOfficerType.Company,
          CompanyOfficerCompany(
            "Some Company",
            true,
            Some("123456789"),
            None,
            "Company Secretary"
          )
        ) -> ListWithTrackedChanges.Updated
      ),
      List(
        CompanyOfficer(
          CompanyOfficerType.Company,
          CompanyOfficerCompany(
            "Some Other Company",
            true,
            Some("123456789"),
            None,
            "Company Secretary"
          )
        )
      ),
      false
    ),
    BusinessStatus(true, Some(LocalDate.of(2018, 6, 30))),
    ImportingActivities(true, Some(EoriNumber("1234123132", true))),
    BusinessCustomers("Over 100"),
    OtherStoragePremises(
      true,
      ListWithTrackedChanges(
        List(
          StoragePremise(Address("1 Some High Street", None, None, Some("Sometown"), "Z99 2YY", None, None), false) -> ListWithTrackedChanges.Added,
          StoragePremise(Address("25 Testing Close", None, None, Some("Othertown"), "Z9 3WW", None, None), true)    -> ListWithTrackedChanges.Updated,
          StoragePremise(Address("26 Testing Close", None, None, Some("Othertown"), "Z9 3WW", None, None), true)    -> ListWithTrackedChanges.NoChange
        ),
        List(StoragePremise(Address("27 Testing Close", None, None, Some("Othertown"), "Z9 3WW", None, None), true)),
        false
      )
    )
  )

  val verifiedEmail = "a@w.ro"

  val declaration = Declaration(
    "C",
    "D",
    false,
    Some("cosmin@cosmin.co.uk"),
    Some(
      AlternativeEmail(
        "a@b.co",
        "a@b.co"
      ))
  )
}
