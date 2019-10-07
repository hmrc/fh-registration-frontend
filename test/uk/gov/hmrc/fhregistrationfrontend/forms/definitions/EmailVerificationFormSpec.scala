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

import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.forms.models.EmailVerification
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class EmailVerificationFormSpec extends UnitSpec with FormSpecsHelper[EmailVerification] {

  override def form: Form[EmailVerification] = EmailVerificationForm.emailVerificationForm
  import EmailVerificationForm._

  "Email Verification Form" should {
    "Fail if usingDefaultEmail is not answered" in {
      formDataHasErrors(
        Map.empty,
        Seq(emailOptionKey → "error.required")
      )
    }

    "Fail if usingDefaultEmail is no and no alternate email is provided" in {
      formDataHasErrors(
        Map(emailOptionKey → "false"),
        Seq(alternativeEmailKey → "error.required")
      )
    }

    "Fail if usingDefaultEmail is no and alternate email is malformed" in {
      formDataHasErrors(
        Map(
          emailOptionKey → "false",
          alternativeEmailKey → "some email"
        ),
        Seq(alternativeEmailKey → "error.email")
      )
    }

    "Parse when using default email" in {
      val parsed = dataFromValidForm(Map(
        emailOptionKey → "true",
        defaultEmailKey → "default@test.com"
      ))

      parsed.usingGgEmailAddress shouldBe true
      parsed.ggEmail shouldBe Some("default@test.com")
      parsed.email shouldBe "default@test.com"
    }

    "Parse when using alternate email" in {
      val parsed = dataFromValidForm(Map(
        emailOptionKey → "false",
        alternativeEmailKey → "alternate@test.com"
      ))

      parsed.usingGgEmailAddress shouldBe false
      parsed.alternativeEmail shouldBe Some("alternate@test.com")
      parsed.email shouldBe "alternate@test.com"
    }
  }

}
