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

package uk.gov.hmrc.fhregistrationfrontend.forms.confirmation

import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.FormSpecsHelper
import uk.gov.hmrc.fhregistrationfrontend.forms.models.AlternativeEmail
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class ConfirmationFormSpec extends UnitSpec with FormSpecsHelper[Confirmation] {

  override def form: Form[Confirmation] = ConfirmationForm.confirmationForm
  import ConfirmationForm._

  "Confirmation form" should {
    "Have errors when no answer is given" in {
      formDataHasErrors(
        Map.empty,
        Seq(
          confirmKey -> "error.required"
        )
      )
    }

    "Have errors when yes but 'use default email' is not answered" in {
      formDataHasErrors(
        Map(confirmKey -> "true"),
        Seq(
          usingDefaultEmailKey -> "error.required"
        )
      )
    }

    "Have errors when default email is missing" in {
      formDataHasErrors(
        Map(confirmKey -> "true", usingDefaultEmailKey -> "true"),
        Seq(
          defaultEmailKey -> "error.required"
        )
      )
    }

    "Have errors when default email is wrong format" in {
      formDataHasErrors(
        Map(
          confirmKey           -> "true",
          usingDefaultEmailKey -> "true",
          defaultEmailKey      -> "not an email"
        ),
        Seq(
          defaultEmailKey -> "error.email"
        )
      )
    }

    "Have errors when no custom email is provided" in {
      formDataHasErrors(
        Map(
          confirmKey           -> "true",
          usingDefaultEmailKey -> "false"
        ),
        Seq(
          s"$alternativeEmailKey.email" -> "error.required"
        )
      )
    }

    "Have errors when no custom email does not match" in {
      formDataHasErrors(
        Map(
          confirmKey                                -> "true",
          usingDefaultEmailKey                      -> "false",
          s"$alternativeEmailKey.email"             -> "a@test.com",
          s"$alternativeEmailKey.emailConfirmation" -> "b@test.com"
        ),
        Seq(
          s"$alternativeEmailKey.emailConfirmation" -> "no_match.error"
        )
      )
    }

    "Accept a NO answer" in {
      val data = Map(
        confirmKey -> "false"
      )

      dataFromValidForm(data).continue shouldBe false
    }

    "Accept a default email" in {
      val data = Map(
        confirmKey           -> "true",
        usingDefaultEmailKey -> "true",
        defaultEmailKey      -> "default@test.com"
      )

      val confirmation = dataFromValidForm(data)
      confirmation.continue shouldBe true
      confirmation.usingDefaultEmail shouldBe Some(true)
      confirmation.defaultEmail shouldBe Some("default@test.com")
      confirmation.alternativeEmail shouldBe None
      confirmation.email shouldBe Some("default@test.com")
    }

    "Accept a custom email" in {
      val data = Map(
        confirmKey                                -> "true",
        usingDefaultEmailKey                      -> "false",
        s"$alternativeEmailKey.email"             -> "custom@test.com",
        s"$alternativeEmailKey.emailConfirmation" -> "custom@test.com"
      )

      val confirmation = dataFromValidForm(data)
      confirmation.continue shouldBe true
      confirmation.usingDefaultEmail shouldBe Some(false)
      confirmation.defaultEmail shouldBe None
      confirmation.alternativeEmail shouldBe Some(AlternativeEmail("custom@test.com"))
      confirmation.email shouldBe Some("custom@test.com")
    }

  }
}
