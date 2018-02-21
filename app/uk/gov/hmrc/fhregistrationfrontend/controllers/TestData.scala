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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import java.time.LocalDate

import uk.gov.hmrc.fhregistrationfrontend.forms.models._

object TestData {
  val mockSummary: LimitedCompanyApplication = LimitedCompanyApplication(
    MainBusinessAddress(
      "Less than 3 years",
      Some(false),
      None
    ),
    ContactPerson(
      "Alistair",
      "Macdonald",
      "Director",
      "07817777841",
      "ali@nubz.com",
      false,
      None,
      None,
      None
    ),
    CompanyRegistrationNumber(
      "12345678"
    ),
    DateOfIncorporation(
      LocalDate.now()
    ),
    TradingName(
      true,
      Some("FulfilStuff4U")
    ),
    VatNumber(
      true,
      Some("19911234")
    ),
    null,
    BusinessStatus(
      true,
      Some(LocalDate.now())
    ),
    ImportingActivities(
      false,
      None
    ),
    BusinessCustomers(
      "11 to 50"
    ),
    OtherStoragePremises(
      false,
      None
    ))
}
