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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class BusinessTypeFormSpec extends UnitSpec with FormSpecsHelper[BusinessType] {

  override def form: Form[BusinessType] = BusinessTypeForm.businessTypeForm
  import BusinessTypeForm._

  "Business type form" should {
    "Fail if field not present" in {
      formRequires(businessTypeKey)
    }

    "Fail if field value is malformed" in {
      formDataHasErrors(Map(businessTypeKey -> "some unknown business type"), Seq(businessTypeKey -> "error.invalid"))
    }

    "Work for known field value" in {
      dataFromValidForm(Map(businessTypeKey -> "CorporateBody")) shouldBe BusinessType.CorporateBody
    }
  }

}
