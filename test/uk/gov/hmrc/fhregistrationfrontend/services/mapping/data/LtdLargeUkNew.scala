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

import uk.gov.hmrc.fhregistrationfrontend.forms.models.ListWithTrackedChanges.Added
import uk.gov.hmrc.fhregistrationfrontend.forms.models._

object LtdLargeUkNew {
  def application(changeFlags: ListWithTrackedChanges.Status = Added) =
    LimitedCompanyApplication(
      MainBusinessAddress(
        "3-5 years",
        None,
        None,
        None
      ),
      ContactPerson(
        "Yuan",
        "ZHANG",
        "Director",
        "11123",
        None,
        false,
        Some(true),
        Some(Address("New Flat", None, None, Some("Testtown"), "ZZ11 1AA", None, None)),
        None
      ),
      CompanyRegistrationNumber("87654321"),
      DateOfIncorporation(LocalDate.of(2014, 5, 21)),
      TradingName(true, Some("CatCo")),
      VatNumber(true, Some("987654321")),
      ListWithTrackedChanges(
        List(
          CompanyOfficer(
            CompanyOfficerType.Individual,
            CompanyOfficerIndividual(
              "Yuan",
              "Zhang",
              true,
              Some("AA123123A"),
              None,
              None,
              None,
              "Company Secretary"
            )) -> changeFlags,
          CompanyOfficer(
            CompanyOfficerType.Individual,
            CompanyOfficerIndividual(
              "Vlad",
              "M",
              true,
              Some("AA122222"),
              None,
              None,
              None,
              "Director"
            )
          ) -> changeFlags
        ),
        List.empty,
        false
      ),
      BusinessStatus(true, Some(LocalDate.of(2018, 7, 30))),
      ImportingActivities(false, None),
      BusinessCustomers("51-100"),
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
    "Y",
    "Z",
    false,
    Some("yuan@yuan.co.uk"),
    Some(
      AlternativeEmail(
        "a@b.co",
        "a@b.co"
      ))
  )
}
