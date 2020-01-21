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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import uk.gov.hmrc.fhregistrationfrontend.forms.models.{CompanyOfficer, CompanyOfficerCompany, CompanyOfficerIndividual}
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class CompanyOfficersFormSpecs extends UnitSpec with FormSpecsHelper[CompanyOfficer] {

  override val form = CompanyOfficersForm.companyOfficerForm

  val validIndividual = Map(
    "firstName" → "George",
    "lastName" → "Costanza",
    "hasNationalInsuranceNumber" → "true",
    "nationalInsuranceNumber" → "AA123123A",
    "role" → "Director"
  )

  "Company Officer as Individual" should {
    "reject when missing values" in {
      val required = Seq(
        "firstName",
        "lastName",
        "hasNationalInsuranceNumber",
        "role"
      ) map { _ → "error.required" }

      formDataHasErrors(
        individual(),
        individualErrors(required: _*)
      )
    }

    "reject when no nino" in {
      formDataHasErrors(
        individual("hasNationalInsuranceNumber" → "true"),
        individualErrors("nationalInsuranceNumber" → "error.required")
      )
    }

    "reject when no hasPassportNumber" in {
      formDataHasErrors(
        individual("hasNationalInsuranceNumber" → "false"),
        individualErrors("hasPassportNumber" → "error.required")
      )
    }

    "reject when no passportNumber" in {
      formDataHasErrors(
        individual(
          "hasNationalInsuranceNumber" → "false",
          "hasPassportNumber" → "true"
        ),
        individualErrors("passportNumber" → "error.required")
      )
    }

    "reject when no nationalId" in {
      formDataHasErrors(
        individual(
          "hasNationalInsuranceNumber" → "false",
          "hasPassportNumber" → "false"
        ),
        individualErrors("nationalID" → "error.required")
      )
    }

    "reject when wrong role" in {
      formDataHasErrors(
        individual(
          "role" → "not valid"
        ),
        individualErrors("role" → "error.invalid")
      )
    }

    "accept valid" in {
      val data = dataFromValidForm(individual(validIndividual.toSeq: _*))
      data.identification match {
        case v: CompanyOfficerIndividual ⇒
          v.firstName shouldBe "George"
          v.lastName shouldBe "Costanza"
          v.hasNino shouldBe true
          v.nino shouldBe Some("AA123123A")
        case _ ⇒ false shouldBe true
      }

    }
  }

  "Company Officer as Company" should {
    "reject when missing values" in {
      val required = Seq(
        "companyName",
        "hasVat",
        "role"
      ) map { _ → "error.required" }

      formDataHasErrors(
        company(),
        companyErrors(required: _*)
      )
    }

    "reject when no vat" in {
      formDataHasErrors(
        company("hasVat" → "true"),
        companyErrors("vatRegistration" → "error.required")
      )
    }

    "reject when no company registration" in {
      formDataHasErrors(
        company("hasVat" → "false"),
        companyErrors("companyRegistration" → "error.required")
      )
    }

    "accept valid data" in {
      val data = dataFromValidForm(
        company(
          "companyName" → "Co co",
          "hasVat" → "true",
          "vatRegistration" → "123123123",
          "role" → "Director"
        )
      )
      data.identification match {
        case _: CompanyOfficerIndividual ⇒ false shouldBe true
        case v: CompanyOfficerCompany ⇒
          v.companyName shouldBe "Co co"
          v.hasVat shouldBe true
          v.vat shouldBe Some("123123123")
          v.role shouldBe "Director"
      }

    }

  }

  def individualErrors(errors: (String, String)*) =
    errors map { case (k, v) ⇒ s"individualIdentification.$k" -> v } toList

  def individual(data: (String, String)*) =
    (data map { case (k, v) ⇒ s"individualIdentification.$k" -> v }).toMap + ("identificationType" → "Individual")

  def companyErrors(errors: (String, String)*) =
    errors map { case (k, v) ⇒ s"companyIdentification.$k" -> v } toList

  def company(data: (String, String)*) =
    (data map { case (k, v) ⇒ s"companyIdentification.$k" -> v }).toMap + ("identificationType" → "Company")

}
