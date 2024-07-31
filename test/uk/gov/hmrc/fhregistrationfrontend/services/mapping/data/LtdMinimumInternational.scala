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

object LtdMinimumInternational {

  def application(changeFlags: ListWithTrackedChanges.Status = Added) = LimitedCompanyApplication(
    MainBusinessAddress(
      "3 to 5 years",
      None,
      None,
      None
    ),
    ContactPerson(
      "C",
      "M",
      "D",
      "123123",
      Some(verifiedEmail),
      false,
      Some(false),
      None,
      Some(
        InternationalAddress(
          "some street",
          Some("Sofia"),
          None,
          "Bulgaria",
          "BG"
        )
      )
    ),
    CompanyRegistrationNumber("12345678", None),
    DateOfIncorporation(LocalDate.of(2009, 4, 21)),
    TradingName(false, None),
    VatNumber(false, None),
    ListWithTrackedChanges(
      List(
        CompanyOfficer(
          CompanyOfficerType.Individual,
          CompanyOfficerIndividual(
            "R",
            "G",
            false,
            None,
            Some(true),
            Some("1231231"),
            None,
            "Company Secretary"
          )
        ) -> changeFlags
      ),
      List.empty,
      false
    ),
    BusinessStatus(false, None),
    ImportingActivities(false, None),
    BusinessCustomers("11-50"),
    OtherStoragePremises(false, ListWithTrackedChanges.empty[StoragePremise]())
  )

  val verifiedEmail = "a@a.ro"

  val declaration = Declaration(
    "R",
    "F",
    true,
    Some("cosmin@cosmin.co.uk"),
    None
  )
}
