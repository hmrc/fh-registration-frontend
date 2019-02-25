/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import uk.gov.hmrc.fhregistrationfrontend.forms.TestData
import uk.gov.hmrc.fhregistrationfrontend.forms.models.ContactPerson
import uk.gov.hmrc.play.test.UnitSpec

class ContactPersonFormSpecs extends UnitSpec with FormSpecsHelper[ContactPerson] {

  val form = ContactPersonForm.contactPersonForm
  import ContactPersonForm._

  val validShort = Map(
    firstNameKey → "George",
    lastNameKey → "Costanza",
    jobTitleKey → "Parking Attendant",
    telephoneKey → "07012311234",
    emailAddressKey → "george.costanza@seinfeld.org",
    usingSameContactAddressKey → "true"
  )

  val dataWithOtherAddress = validShort +
    (usingSameContactAddressKey → "false")

  val validWithUkAddress = dataWithOtherAddress +
    (isUkAddressKey → "true") ++
    TestData.addressDataUk(otherUkContactAddressKey)

  "Contact person form" should {

    "reject data with other address true but no address" in {
      formDataHasErrors(
        dataWithOtherAddress,
        List(isUkAddressKey → "error.required")
      )
    }

    "reject data with other uk address but no address" in {
      formDataHasErrors(
        dataWithOtherAddress + (isUkAddressKey → "true"),
        List(s"$otherUkContactAddressKey.Line1"  → "error.required")
      )
    }

    "reject data with other international address but no address" in {
      formDataHasErrors(
        dataWithOtherAddress + (isUkAddressKey → "false"),
        List(s"$otherInternationalContactAddressKey.Line1" → "error.required")
      )
    }

    "reject when missing fields" in {
      formDataHasErrors(
        Map(),
        List(
          firstNameKey → "error.required",
          lastNameKey → "error.required",
          jobTitleKey → "error.required",
          telephoneKey → "error.required",
          usingSameContactAddressKey → "error.required"
        )
      )
    }

    "reject wrong fields" in {
      fieldHasErrors(firstNameKey, "", "error.pattern")
      fieldHasErrors(lastNameKey, "", "error.pattern")

      fieldHasErrors(firstNameKey, Array.fill(36)('a').mkString, "error.pattern")
      fieldHasErrors(lastNameKey, Array.fill(36)('a').mkString, "error.pattern")

      for (w ← Seq("@", "#")) {
        fieldHasErrors(firstNameKey, w, "error.pattern")
        fieldHasErrors(lastNameKey, w, "error.pattern")
      }

      fieldHasErrors(jobTitleKey, "", "error.pattern")
      fieldHasErrors(jobTitleKey, Array.fill(41)('a').mkString, "error.pattern")
      fieldHasErrors(jobTitleKey, "@", "error.pattern")


      fieldHasErrors(telephoneKey, "", "error.pattern")

      fieldHasErrors(telephoneKey, "", "error.pattern")
      fieldHasErrors(telephoneKey, Array.fill(25)('7').mkString, "error.pattern")
      fieldHasErrors(telephoneKey, "a", "error.pattern")

      fieldHasErrors(emailAddressKey, "user", "error.email")
      //max 132
      fieldHasErrors(emailAddressKey, "user@" + Array.fill(128)('a').mkString, "error.email")

    }

    "accept valid short form" in {
      val data = dataFromValidForm(validShort)

      data.firstName shouldBe "George"
      data.lastName shouldBe "Costanza"
      data.jobTitle shouldBe "Parking Attendant"
      data.telephone shouldBe "07012311234"
      data.emailAddress shouldBe Some("george.costanza@seinfeld.org")
      data.usingSameContactAddress shouldBe true
      data.otherUkContactAddress shouldBe None
      data.otherInternationalContactAddress shouldBe None
    }

    "accept valid with uk address" in {
      val data = dataFromValidForm(validWithUkAddress)

      data.usingSameContactAddress shouldBe false
      data.otherUkContactAddress shouldBe Some(TestData.addressUk)
      data.otherInternationalContactAddress shouldBe None
    }
  }

}
