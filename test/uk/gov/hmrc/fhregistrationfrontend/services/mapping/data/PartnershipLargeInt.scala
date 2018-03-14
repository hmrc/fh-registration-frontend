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

object PartnershipLargeInt {
  def application(changeFlags: ListWithTrackedChanges.Status = Added) = PartnershipBusinessApplication(
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
      Some(false),
      None,
      Some(InternationalAddress(
        "some street",
        Some("Sofia"),
        None,
        "Bulgaria",
        "BG"
      ))
    ),
    TradingName(true, Some("DodgyCo")),
    VatNumber(true, Some("123456789")),
    ListWithTrackedChanges(
      List(BusinessPartner(
        BusinessPartnerType.Individual,
        BusinessPartnerIndividual(
          "mr partner",
          "ship",
          true,
          Some("AA123123A"),
          Address(
            "individual line one",
            None,
            None,
            Some("Othertown"),
            "Z9 3WW",
            None)
        )) → changeFlags,
        BusinessPartner(
          BusinessPartnerType.SoleProprietor,
          BusinessPartnerSoleProprietor(
            "ms sole",
            "trader",
            true,
            Some("dodgy sole trader"),
            true,
            Some("AA123231"),
            true,
            Some("223456789"),
            None,
            Address(
              "sole line one",
              None,
              None,
              Some("sole town"),
              "AA13 1AA",
              None)
          )
        ) → changeFlags,
        BusinessPartner(
          BusinessPartnerType.LimitedLiabilityPartnership,
          BusinessPartnerLimitedLiabilityPartnership(
            "fulfilment llp",
            true,
            Some("dodgy llp"),
            "SC123456",
            true,
            Some("323456789"),
            None,
            Address(
              "llp line one",
              Some("llp line two"),
              Some("llp line three"),
              Some("llp town"),
              "AA14 1AA",
              None)
          )
        ) → changeFlags,
        BusinessPartner(
          BusinessPartnerType.Partnership,
          BusinessPartnerPartnership(
            "fulfilment partNers",
            true,
            Some("dodgy partners"),
            true,
            Some("423456789"),
            true,
            Some("2234567890"),
            Address(
              "partner line one",
              Some("partner line two"),
              None,
              Some("partner town"),
              "AA15 1AA",
              None)
          )
        ) → changeFlags,
        BusinessPartner(
          BusinessPartnerType.CorporateBody,
          BusinessPartnerCorporateBody(
            "fulfilment ltd",
            true,
            Some("dodgy ltd"),
            "12345679",
            false,
            None,
            Some("1234567890"),
            Address(
              "ltd line one",
              None,
              None,
              Some("ltd town"),
              "AA16 1AA",
              None)
          )
        ) → changeFlags,
        BusinessPartner(
          BusinessPartnerType.UnincorporatedBody,
          BusinessPartnerUnincorporatedBody(
            "church of fulfilment",
            true,
            Some("dodgy church"),
            false,
            None,
            false,
            None,
            Address(
              "church one",
              None,
              None,
              Some("church town"),
              "AA17 1AA",
              None)
          )
        ) → changeFlags),
      List.empty),
    BusinessStatus(true, Some(LocalDate.of(2018,8,31))),
    ImportingActivities(true, Some(EoriNumber("1234123132", true))),
    BusinessCustomers("Over 100"),
    OtherStoragePremises(true,
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
            false) → changeFlags,
          StoragePremise(
            Address(
              "25 Testing Close",
              None,
              None,
              Some("Othertown"),
              "Z9 3WW",
              None),
            true) → changeFlags),
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
