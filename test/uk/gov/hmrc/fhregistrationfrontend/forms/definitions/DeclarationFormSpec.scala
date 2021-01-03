/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Declaration
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class DeclarationFormSpec extends UnitSpec with FormSpecsHelper[Declaration] {

  override def form: Form[Declaration] = DeclarationForm.declarationForm
  import DeclarationForm._

  val personsData = Map(
    fullNameKey → "Full Name",
    jobTitleKey → "Director"
  )

  "Declaration form" should {
    "Always require some fields" in {
      formRequires(fullNameKey, jobTitleKey, usingDefaultEmailKey)
    }

    "Fail if using default email but no default email present" in {
      formDataHasErrors(
        personsData ++ Map(usingDefaultEmailKey → "true"),
        Seq(defaultEmailKey → "error.required")
      )
    }

    "Fail if alternative email but no email present" in {
      formDataHasErrors(
        personsData ++ Map(usingDefaultEmailKey → "false"),
        Seq(s"$alternativeEmailKey.email" → "error.required")
      )
    }

    "Fail if alternative email but email is malformed" in {
      formDataHasErrors(
        personsData ++ Map(usingDefaultEmailKey → "false", s"$alternativeEmailKey.email" → "malformed"),
        Seq(s"$alternativeEmailKey.email" → "error.email")
      )
    }

    "Fail if alternative email but email does not match" in {
      formDataHasErrors(
        personsData ++ Map(
          usingDefaultEmailKey → "false",
          s"$alternativeEmailKey.email" → "alternative@test.com",
          s"$alternativeEmailKey.emailConfirmation" → "another@test.com"),
        Seq(s"$alternativeEmailKey.emailConfirmation" → "no_match.error")
      )
    }

    "Accept data using default email" in {
      val parsed = dataFromValidForm(
        personsData ++ Map(
          usingDefaultEmailKey → "true",
          defaultEmailKey → "default@test.com"
        )
      )

      parsed.fullName shouldBe "Full Name"
      parsed.jobTitle shouldBe "Director"
      parsed.usingDefaultEmail shouldBe true
      parsed.email shouldBe "default@test.com"
    }

    "Accept data using alternative email" in {
      val parsed = dataFromValidForm(
        personsData ++ Map(
          usingDefaultEmailKey → "false",
          s"$alternativeEmailKey.email" → "alternative@test.com",
          s"$alternativeEmailKey.emailConfirmation" → "alternative@test.com")
      )

      parsed.fullName shouldBe "Full Name"
      parsed.jobTitle shouldBe "Director"
      parsed.usingDefaultEmail shouldBe false
      parsed.email shouldBe "alternative@test.com"
    }

  }

}
