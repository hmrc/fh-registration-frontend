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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import uk.gov.hmrc.fhregistrationfrontend.forms.models.DateOfIncorporation
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class DateOfIncorporationSpecs extends UnitSpec {
  val form = DateOfIncorporationForm.dateOfIncorporationForm
  val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  "DateOfIncorporationForm form binding" should {

    "validate a form with date" in {
      val data = Map(
        "dateOfIncorporation.day"   -> "31",
        "dateOfIncorporation.month" -> "7",
        "dateOfIncorporation.year"  -> "2015"
      )
      val result = form.bind(data).value.get
      result shouldBe DateOfIncorporation(LocalDate.parse("31/07/2015", dtf))
    }

    "validate a form with day missing" in {
      val data = Map(
        "dateOfIncorporation.month" -> "7",
        "dateOfIncorporation.year"  -> "2015"
      )
      val result = form.bind(data)
      val errors = result.errors.flatMap(v => v.messages.map(m => v.key -> m))
      errors should contain("dateOfIncorporation.day" -> "error.required")

      result.value shouldBe None
    }

    "validate a form with month missing" in {
      val data = Map(
        "dateOfIncorporation.day"   -> "7",
        "dateOfIncorporation.month" -> "",
        "dateOfIncorporation.year"  -> "2015"
      )
      val result = form.bind(data)
      val errors = result.errors.flatMap(v => v.messages.map(m => v.key -> m))
      errors should contain("dateOfIncorporation" -> "month.missing")

      result.value shouldBe None
    }

    "validate a form with year missing" in {
      val data = Map(
        "dateOfIncorporation.day"   -> "7",
        "dateOfIncorporation.month" -> "3",
        "dateOfIncorporation.year"  -> ""
      )
      val result = form.bind(data)
      val errors = result.errors.flatMap(v => v.messages.map(m => v.key -> m))
      errors should contain("dateOfIncorporation" -> "year.missing")

      result.value shouldBe None
    }

    "validate a form with day and year missing" in {
      val data = Map(
        "dateOfIncorporation.day"   -> "",
        "dateOfIncorporation.month" -> "3",
        "dateOfIncorporation.year"  -> ""
      )
      val result = form.bind(data)
      val errors = result.errors.flatMap(v => v.messages.map(m => v.key -> m))
      errors should contain("dateOfIncorporation" -> "day-and-year.missing")

      result.value shouldBe None
    }

    "validate a form with day and month missing" in {
      val data = Map(
        "dateOfIncorporation.day"   -> "",
        "dateOfIncorporation.month" -> "",
        "dateOfIncorporation.year"  -> "2016"
      )
      val result = form.bind(data)
      val errors = result.errors.flatMap(v => v.messages.map(m => v.key -> m))
      errors should contain("dateOfIncorporation" -> "day-and-month.missing")

      result.value shouldBe None
    }
    "validate a form with month and year missing" in {
      val data = Map(
        "dateOfIncorporation.day"   -> "4",
        "dateOfIncorporation.month" -> "",
        "dateOfIncorporation.year"  -> ""
      )
      val result = form.bind(data)
      val errors = result.errors.flatMap(v => v.messages.map(m => v.key -> m))
      errors should contain("dateOfIncorporation" -> "month-and-year.missing")

      result.value shouldBe None
    }

    "validate a form with invalid date" in {
      val data = Map(
        "dateOfIncorporation.day"   -> "30",
        "dateOfIncorporation.month" -> "2",
        "dateOfIncorporation.year"  -> "2015"
      )
      val result = form.bind(data)
      val errors = result.errors.flatMap(v => v.messages.map(m => v.key -> m))
      errors should contain("dateOfIncorporation" -> "date.error.invalid")

      result.value shouldBe None
    }

    "validate a form with out of range dates" in {
      val data = Map(
        "dateOfIncorporation.day"   -> "311",
        "dateOfIncorporation.month" -> "7",
        "dateOfIncorporation.year"  -> "2015"
      )
      val result = form.bind(data)

      val errors = result.errors.flatMap(v => v.messages)
      errors should contain("date.error.invalid")
      result.value shouldBe None
    }

  }

}
