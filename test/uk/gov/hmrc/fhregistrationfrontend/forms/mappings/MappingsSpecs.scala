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

package uk.gov.hmrc.fhregistrationfrontend.forms.mappings

import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec
import play.api.data.Forms.*
import org.scalatest.EitherValues
import play.api.data.validation.*
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.*

import java.time.LocalDate

class MappingsSpecs extends UnitSpec with EitherValues {

  "OptionalWithYesAndNo" should {
    "work " in {
      val test = Mappings.optionalWithYesOrNo(number).withPrefix("prefix")

      test.bind(Map("prefix.value" -> "123", "prefix.yesNo" -> "true")) shouldEqual Right(Some(123))
      test.bind(Map("prefix.yesNo" -> "false")) shouldEqual Right(None)

      test.bind(Map("prefix.value" -> "123", "prefix.yesNo" -> "false")).swap.getOrElse(Seq.empty) should have length 1
      test.bind(Map("prefix.value" -> "123")).swap.getOrElse(Seq.empty) should have length 1

      test.unbind(None) shouldEqual Map("prefix.yesNo" -> "false")
      test.unbind(Some(123)) shouldEqual Map("prefix.yesNo" -> "true", "prefix.value" -> "123")

    }
  }

  "dateInPast" should {
    "return Valid for a date in the past" in {
      val yesterday = LocalDate.now().minusDays(1)
      dateInPast(
        yesterday.getDayOfMonth.toString,
        yesterday.getMonthValue.toString,
        yesterday.getYear.toString
      ) shouldEqual Valid
    }

    "return Valid for today's date" in {
      val today = LocalDate.now()
      dateInPast(
        today.getDayOfMonth.toString,
        today.getMonthValue.toString,
        today.getYear.toString
      ) shouldEqual Valid
    }

    "return Invalid for a date in the future" in {
      val tomorrow = LocalDate.now().plusDays(1)
      dateInPast(
        tomorrow.getDayOfMonth.toString,
        tomorrow.getMonthValue.toString,
        tomorrow.getYear.toString
      ) shouldBe an[Invalid]
    }
  }

}
