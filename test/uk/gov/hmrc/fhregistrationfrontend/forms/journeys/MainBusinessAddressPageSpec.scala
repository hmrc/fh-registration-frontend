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

package uk.gov.hmrc.fhregistrationfrontend.forms.journeys

import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.MainBusinessAddressForm
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, MainBusinessAddress, PreviousAddress}
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

import java.time.LocalDate

class MainBusinessAddressPageSpec extends UnitSpec with MockitoSugar {

  private val mainPage = BasicPage(
    "years-at-current-address",
    MainBusinessAddressForm.timeAtCurrentAddressForm,
    mock[FormRendering[String]]
  )
  private val hasPreviousAddressPage = BasicPage(
    "any-previous-business-address",
    MainBusinessAddressForm.hasPreviousAddressForm,
    mock[FormRendering[Boolean]]
  )
  private val previousAddressPage = BasicPage(
    "previous-business-address",
    MainBusinessAddressForm.previousAddressForm,
    mock[FormRendering[PreviousAddress]]
  )

  private val anyPreviousBusinessAddressSection = Some("any-previous-business-address")
  private val previousBusinessAddressSection = Some("previous-business-address")

  private val answeredMoreThanThreeYearsToTimeAtCurrentAddress = MainBusinessAddressPage(
    mainPage `withData` MainBusinessAddress.TimeAtCurrentAddressOptions.last,
    hasPreviousAddressPage,
    previousAddressPage
  )

  private val answeredLessThanThreeYearsToTimeAtCurrentAddress = MainBusinessAddressPage(
    mainPage `withData` MainBusinessAddress.TimeAtCurrentAddressOptions.head,
    hasPreviousAddressPage,
    previousAddressPage
  )

  private val answeredLessThanThreeYearsToTimeAtCurrentAddressAndHasPreviousAddressFalse = MainBusinessAddressPage(
    mainPage `withData` MainBusinessAddress.TimeAtCurrentAddressOptions.head,
    hasPreviousAddressPage `withData` false,
    previousAddressPage
  ).withSubsection(anyPreviousBusinessAddressSection)

  private val answeredLessThanThreeYearsToTimeAtCurrentAddressAndHasPreviousAddressTrue = MainBusinessAddressPage(
    mainPage `withData` MainBusinessAddress.TimeAtCurrentAddressOptions.head,
    hasPreviousAddressPage `withData` true,
    previousAddressPage
  ).withSubsection(anyPreviousBusinessAddressSection)

  private val previousAddress = PreviousAddress(
    Address("Flat 1A", None, None, Some("Testtown"), "ZZ11 1ZZ", None, None),
    LocalDate.of(2015, 3, 20)
  )

  private val answeredLessThanThreeYearsToTimeAtCurrentAddressAndPreviousAddressInputted = MainBusinessAddressPage(
    mainPage `withData` MainBusinessAddress.TimeAtCurrentAddressOptions.head,
    hasPreviousAddressPage `withData` true,
    previousAddressPage `withData` previousAddress
  ).withSubsection(previousBusinessAddressSection)

  "Next section" should {
    "Be None" when {
      "Answer is more than three years on time at current address section" in {
        answeredMoreThanThreeYearsToTimeAtCurrentAddress.nextSubsection shouldBe None
      }

      "Answer is No on any previous address subsection" in {
        answeredLessThanThreeYearsToTimeAtCurrentAddressAndHasPreviousAddressFalse.nextSubsection shouldBe None
      }

      "on previous address subsection" in {
        answeredLessThanThreeYearsToTimeAtCurrentAddressAndPreviousAddressInputted.nextSubsection shouldBe None
      }
    }

    "Be any previous address subsection" when {
      "Answer is Yes on main section" in {
        answeredLessThanThreeYearsToTimeAtCurrentAddress.nextSubsection.get shouldEqual anyPreviousBusinessAddressSection.get
      }
    }

    "Be previous address subsection" when {
      "Answer is Yes on any previous address subsection" in {
        answeredLessThanThreeYearsToTimeAtCurrentAddressAndHasPreviousAddressTrue.nextSubsection.get shouldBe previousBusinessAddressSection.get
      }
    }

  }

  "Previous section" should {
    "Be none" when {
      "Answer is more than three years on time at current address section" in {
        answeredMoreThanThreeYearsToTimeAtCurrentAddress.previousSubsection shouldBe None
      }

      "Answer is less than three years on main section" in {
        answeredLessThanThreeYearsToTimeAtCurrentAddress.previousSubsection shouldBe None
      }
    }

    "Be time at current address section" when {
      "on any previous address section and answered less than three years on time at current address section - true" in {
        answeredLessThanThreeYearsToTimeAtCurrentAddressAndHasPreviousAddressTrue.previousSubsection.get shouldEqual "years-at-current-address"
      }

      "on any previous address section and answered less than three years on time at current address section - false" in {
        answeredLessThanThreeYearsToTimeAtCurrentAddressAndHasPreviousAddressFalse.previousSubsection.get shouldEqual "years-at-current-address"
      }
    }

    "Be any previous address subsection" when {
      "on previous address subsection" in {
        answeredLessThanThreeYearsToTimeAtCurrentAddressAndPreviousAddressInputted.previousSubsection.get shouldEqual anyPreviousBusinessAddressSection.get
      }
    }

  }

}
