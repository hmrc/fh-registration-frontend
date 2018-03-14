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

package uk.gov.hmrc.fhregistrationfrontend.services.mapping.data

import java.time.LocalDate

import uk.gov.hmrc.fhregistrationfrontend.forms.models.ListWithTrackedChanges.Added
import uk.gov.hmrc.fhregistrationfrontend.forms.models._

object LtdMinimum {
  def application(changeFlags: ListWithTrackedChanges.Status = Added) = LimitedCompanyBusinessApplication(
    MainBusinessAddress(
      "3-5 years",
      None,
      None,
      None
    ),
    ContactPerson(
      "C",
      "M",
      "director",
      "07231111",
      "a@a.ro",
      true,
      None,
      None,
      None
    ),
    CompanyRegistrationNumber("12345678"),
    DateOfIncorporation(LocalDate.of(2015, 3, 20)),
    TradingName(false, None),
    VatNumber(false, None),
    ListWithTrackedChanges(
      List(
        CompanyOfficer(
          CompanyOfficerType.Individual,
          CompanyOfficerIndividual(
            "C",
            "M",
            true,
            Some("AA123456A"),
            None, None, None,
            "Director"
          )
        ) → changeFlags),
      List.empty),
    BusinessStatus(false, None),
    ImportingActivities(false, None),
    BusinessCustomers("1-10"),
    OtherStoragePremises(false, ListWithTrackedChanges.empty[StoragePremise])
  )

  val declaration = Declaration(
    "C",
    "Director",
    false,
    None,
    Some(AlternativeEmail(
      "a@a.ro",
      "a@a.ro"
    ))
  )
}
