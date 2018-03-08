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

import uk.gov.hmrc.fhregistrationfrontend.forms.models._

object SPLargeUk {
  val application = SoleProprietorApplication(
    MainBusinessAddress(
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
    ),
    ContactPerson(
      "Cosmin",
      "Marian",
      "Director",
      "11123",
      "a@w.ro",
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
    ),
    NationalInsuranceNumber(true, Some("AA123111A")),
    TradingName(true, Some("DodgyCo")),
    VatNumber(true, Some("123456789")),
    BusinessStatus(true, Some(LocalDate.of(2018,6, 30))),
    ImportingActivities(true, Some(EoriNumber("1234123132", true))),
    BusinessCustomers("Over 100"),
    OtherStoragePremises(true,
      ListWithTrackedChanges(
        List(
          StoragePremise(
            Address(
              "two flat",
              Some("two street"),
              None,
              Some("two town"),
              "Z99 2YY",
              None),
            true) → ListWithTrackedChanges.Added,
          StoragePremise(
            Address(
              "25 Testing Close",
              None,
              None,
              Some("Othertown"),
              "AA01 0AA",
              None),
            false) → ListWithTrackedChanges.Added),
        List.empty))
  )

  val declaration = Declaration(
    "C",
    "D",
    false,
    Some("cosmin@cosmin.co.uk"),
    Some(AlternativeEmail(
      "a@b.co",
      "a@b.co"
    ))
  )
}
