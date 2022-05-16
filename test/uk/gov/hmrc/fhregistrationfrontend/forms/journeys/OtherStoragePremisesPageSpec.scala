/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.forms.journeys

import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec
import uk.gov.hmrc.fhregistrationfrontend.forms.TestData
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.StoragePremisesForm
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{ListWithTrackedChanges, StoragePremise}

class OtherStoragePremisesPageSpec extends UnitSpec with MockitoSugar {

  val repeatingPage = RepeatingPage(
    "storagePremises",
    mock[RepeatedFormRendering[(StoragePremise, Boolean)]],
    StoragePremisesForm.storagePremiseMapping
  )
  val mainPage = BasicPage(
    "main",
    StoragePremisesForm.hasOtherStoragePrmisesForm,
    mock[FormRendering[Boolean]]
  )

  "Next section" should {
    "Be None" when {
      "Answer is No" in {
        val page = OtherStoragePremisesPage(mainPage withData false, repeatingPage)

        page.nextSubsection shouldBe None
      }

      "Has more is false" in {
        val page = OtherStoragePremisesPage(mainPage withData true, repeatingPage withData listWithPremises(2))
          .withSubsection(Some("2"))

        page.nextSubsection shouldBe None

      }

    }

    "Be a number" when {
      "Answer is Yes" in {

        val page =
          OtherStoragePremisesPage(mainPage withData true, repeatingPage withData ListWithTrackedChanges.empty())

        page.nextSubsection shouldBe Some("1")
      }

      "Has more is True" in {

        val page = OtherStoragePremisesPage(
          mainPage withData true,
          repeatingPage withData (listWithPremises(2) copy (addMore = true)))
          .withSubsection(Some("2"))

        page.nextSubsection shouldBe Some("3")

      }
    }
  }

  "Previous section" should {
    "Be none" when {
      "on main section" in {
        val page = OtherStoragePremisesPage(
          mainPage withData true,
          repeatingPage withData (listWithPremises(2) copy (addMore = true)))

        page.previousSubsection shouldBe None
      }
    }

    "Be main section" when {
      "on first premise" in {
        val page = OtherStoragePremisesPage(mainPage withData true, repeatingPage withData listWithPremises(2))
          .withSubsection(Some("1"))

        page.previousSubsection shouldBe Some("any")
      }
    }

    "Be first premise" when {
      "on second premise" in {
        val page = OtherStoragePremisesPage(mainPage withData true, repeatingPage withData listWithPremises(2))
          .withSubsection(Some("2"))

        page.previousSubsection shouldBe Some("1")
      }
    }

  }

  def listWithPremises(nPremise: Int) = {
    val aPremise = StoragePremise(TestData.addressUk, false)
    ListWithTrackedChanges.fromValues(List.fill(nPremise)(aPremise))

  }

}
