/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.services.mapping

import java.time.LocalDate

import monocle.{Optional, Prism}
import monocle.Monocle.some
import monocle.macros.{GenLens, GenPrism}
import play.api.libs.json.Json
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.des.{ChangeIndicators, LimitedLiabilityPartnershipCorporateBody, Subscription}
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.data.LtdLargeUk
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class DiffSpecs extends UnitSpec {

  val service = new FormToDesImpl()

  val numberOfCustomersLens = GenLens[LimitedCompanyApplication](_.businessCustomers.numberOfCustomers)
  val vatNumberLens = GenLens[LimitedCompanyApplication](_.vatNumber)
  val importingActivitiesLens = GenLens[LimitedCompanyApplication](_.importingActivities)
  val companyOfficerLens = GenLens[LimitedCompanyApplication](_.companyOfficers)
  val otherStoragePremisesLens = GenLens[LimitedCompanyApplication](_.otherStoragePremises)
  val businessStatusLens = GenLens[LimitedCompanyApplication](_.businessStatus)
  val tradingNameLens = GenLens[LimitedCompanyApplication](_.tradingName)

  "changeIndicators" should {
    "show no change" in {
      val subscription: Subscription = mkSubscription()

      val result = Diff.changeIndicators(subscription, subscription)
      result shouldBe changeIndicators()
    }

    "flag businessTypeChanged" when {
      val businessTypeChanged = changeIndicators(businessTypeChanged = true)
      "is not a new fulfilment" in {
        testChangeIndicators(businessStatusLens set BusinessStatus(false, None), businessTypeChanged)
      }

      "proposed start date changed" in {
        testChangeIndicators(
          businessStatusLens set BusinessStatus(true, Some(LocalDate.of(2018, 7, 30))),
          businessTypeChanged)
      }
    }

    "flag businessDetailChanged" when {
      val businessDetailChanged = changeIndicators(businessDetailChanged = true)
      "trading name is not provided" in {
        testChangeIndicators(tradingNameLens set TradingName(false, None), businessDetailChanged)
      }

      "trading name is changed" in {
        testChangeIndicators(tradingNameLens set TradingName(true, Some("LegitCo")), businessDetailChanged)
      }

      "vat number is not provided" in {
        testChangeIndicators(
          vatNumberLens set VatNumber(false, None),
          changeIndicators(businessDetailChanged = true, additionalBusinessInfoChanged = true))
      }

      "vat number is changed" in {
        testChangeIndicators(vatNumberLens set VatNumber(true, Some("111111111")), businessDetailChanged)
      }
    }

    "flag coOfficialsChanged but not additionalBusinessInfoChanged" in {
      testChangeIndicators(
        companyOfficerLens modify { companyOfficers ⇒
          ListWithTrackedChanges(
            companyOfficers.valuesWithStatus.tail,
            List(companyOfficers.valuesWithStatus.head._1),
            addMore = false)
        },
        changeIndicators(coOfficialsChanged = true)
      )
    }

    "flag premisesChanged but not additionalBusinessInfoChanged" in {
      testChangeIndicators(
        otherStoragePremisesLens modify { otherStoragePremises ⇒
          val entries = otherStoragePremises.value.valuesWithStatus
          OtherStoragePremises(true, ListWithTrackedChanges(entries.tail, List(entries.head._1), addMore = false))
        },
        changeIndicators(premisesChanged = true)
      )
    }

    "flag additionalInfoChanged but not premises or company officer" when {
      val onlyAdditionalInfoChanged = changeIndicators(
        additionalBusinessInfoChanged = true,
        premisesChanged = false,
        coOfficialsChanged = false
      )

      "hasEori is set to false" in {
        testChangeIndicators(importingActivitiesLens set ImportingActivities(false, None), onlyAdditionalInfoChanged)
      }

      "eori number changes" in {
        testChangeIndicators(
          importingActivitiesLens set ImportingActivities(true, Some(EoriNumber("1111111111", true))),
          onlyAdditionalInfoChanged)
      }

      "goods imported outside eori changes" in {
        testChangeIndicators(
          importingActivitiesLens set ImportingActivities(true, Some(EoriNumber("1234123132", false))),
          onlyAdditionalInfoChanged)
      }

      "number of customers changes" in {
        testChangeIndicators(numberOfCustomersLens set "None", onlyAdditionalInfoChanged)
      }

      "no longer declaring vat number" in {
        testChangeIndicators(
          vatNumberLens set VatNumber(false, None),
          changeIndicators(additionalBusinessInfoChanged = true, businessDetailChanged = true))
      }
    }

  }

  private def testChangeIndicators(
    update: LimitedCompanyApplication ⇒ LimitedCompanyApplication,
    expectedChangeIndicators: ChangeIndicators) = {
    val subscription: Subscription = mkSubscription()
    val amendedApplication = update(LtdLargeUk.application(ListWithTrackedChanges.NoChange))

    val amended = mkSubscription(application = amendedApplication)

    val result = Diff.changeIndicators(subscription, amended)
    result shouldBe expectedChangeIndicators
  }

  private def changeIndicators(
    businessTypeChanged: Boolean = false,
    businessDetailChanged: Boolean = false,
    partnersChanged: Boolean = false,
    businessAddressChanged: Boolean = false,
    businessPreviousAddressChanged: Boolean = false,
    contactDetailChanged: Boolean = false,
    coOfficialsChanged: Boolean = false,
    additionalBusinessInfoChanged: Boolean = false,
    premisesChanged: Boolean = false,
    declarationChanged: Boolean = false
  ) =
    ChangeIndicators(
      businessTypeChanged,
      businessDetailChanged,
      partnersChanged,
      businessAddressChanged,
      businessPreviousAddressChanged,
      contactDetailChanged,
      coOfficialsChanged,
      additionalBusinessInfoChanged,
      premisesChanged,
      declarationChanged
    )

  private def brd(fileName: String): BusinessRegistrationDetails =
    Json
      .parse(getClass.getResourceAsStream(s"/models/$fileName"))
      .as[BusinessRegistrationDetails]

  private def mkSubscription(
    application: LimitedCompanyApplication = LtdLargeUk.application(),
    declaration: Declaration = LtdLargeUk.declaration,
    registrationDetails: BusinessRegistrationDetails = brd("business-registration-details-limited-company.json"),
    email: String = LtdLargeUk.verifiedEmail
  ) = service.limitedCompanySubmission(
    registrationDetails,
    email,
    application,
    declaration
  )

}
