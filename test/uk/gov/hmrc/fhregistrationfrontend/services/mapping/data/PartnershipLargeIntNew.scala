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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.ListWithTrackedChanges.{Added, Updated}
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.BusinessPartnerType

object PartnershipLargeIntNew {
  def application(changeFlags: ListWithTrackedChanges.Status = Added) =
    PartnershipApplication(
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
      TradingName(true, Some("CatCo")),
      VatNumber(true, Some("987654321")),
      ListWithTrackedChanges(
        List(
          BusinessPartner(
            BusinessPartnerType.Individual,
            BusinessPartnerIndividual(
              "mr partner",
              "shipIndividual",
              true,
              Some("AA123123A"),
              Address("individual line one", None, None, Some("Othertown"), "Z9 3WW", None, None)
            )
          ) -> Updated,
          BusinessPartner(
            BusinessPartnerType.SoleProprietor,
            BusinessPartnerSoleProprietor(
              "ms sole",
              "traderNew",
              true,
              Some("dodgy sole trader"),
              true,
              Some("AA123231"),
              true,
              Some("223456789"),
              None,
              Address("sole line one", None, None, Some("sole town"), "AA13 1AA", None, None)
            )
          ) -> Updated,
          BusinessPartner(
            BusinessPartnerType.LimitedLiabilityPartnership,
            BusinessPartnerLimitedLiabilityPartnership(
              "fulfilment llp",
              true,
              Some("cat llp"),
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
                None,
                None
              )
            )
          ) -> Updated,
          BusinessPartner(
            BusinessPartnerType.Partnership,
            BusinessPartnerPartnership(
              "fulfilment partNers",
              true,
              Some("cat partners"),
              true,
              Some("423456789"),
              true,
              Some("2234567890"),
              Address("partner line one", Some("partner line two"), None, Some("partner town"), "AA15 1AA", None, None)
            )
          ) -> Updated,
          BusinessPartner(
            BusinessPartnerType.CorporateBody,
            BusinessPartnerCorporateBody(
              "fulfilment ltd",
              true,
              Some("car ltd"),
              "12345679",
              false,
              None,
              Some("1234567890"),
              Address("ltd line one", None, None, Some("ltd town"), "AA16 1AA", None, None)
            )
          ) -> Updated
        ),
        List(
          BusinessPartner(
            BusinessPartnerType.UnincorporatedBody,
            BusinessPartnerUnincorporatedBody(
              "church of fulfilment",
              true,
              Some("cat church"),
              false,
              None,
              false,
              None,
              Address("church one", None, None, Some("church town"), "AA17 1AA", None, None)
            )
          )
        ),
        false
      ),
      BusinessStatus(true, Some(LocalDate.of(2018, 7, 30))),
      ImportingActivities(false, None),
      BusinessCustomers("51-100"),
      OtherStoragePremises(
        true,
        ListWithTrackedChanges(
          List(
            StoragePremise(
              Address("1 Some High Street", None, None, Some("Sometown"), "Z99 2YY", None, None),
              false
            ) -> ListWithTrackedChanges.Added,
            StoragePremise(
              Address("25 Testing Close", None, None, Some("Othertown"), "Z9 3WW", None, None),
              true
            ) -> ListWithTrackedChanges.Updated,
            StoragePremise(
              Address("26 Testing Close", None, None, Some("Othertown"), "Z9 3WW", None, None),
              true
            ) -> ListWithTrackedChanges.NoChange
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
    Some(AlternativeEmail("a@b.co"))
  )
}
