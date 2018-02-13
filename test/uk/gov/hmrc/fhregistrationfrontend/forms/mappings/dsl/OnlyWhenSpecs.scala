/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl

import play.api.data.Forms._
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.MappingSpecsHelper
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl.MappingsApi.{MappingOps, MappingWithKeyOps}
import uk.gov.hmrc.play.test.UnitSpec

class OnlyWhenSpecs extends UnitSpec with MappingSpecsHelper[Option[String]] {

  val conditionMapping = "yesNo" → nonEmptyText(1,10)

  val field = nonEmptyText onlyWhen (conditionMapping is "yes")
  val mapping = field withPrefix "value"

  val validPresent = Map(
    "yesNo" → "yes",
    "value" → "123"
  )

  val validAbsent = Map(
    "yesNo" → "no"
  )

  "OnlyWhen " should {
    "accept yes value" in {
      val data = dataFromValidForm(validPresent)
      data.isDefined shouldBe true
      data.get shouldBe "123"
    }

    "accept no value " in {
      val data = dataFromValidForm(validAbsent)
      data shouldBe None
    }

    "reject with yes but no value" in {
      formDataHasErrors(Map("yesNo" → "yes"), List("value" → "error.required"))
    }

    "reject with no but value" in {
      formDataHasErrors(Map("yesNo" → "no", "value" → "123"), List("value" → "error.unexpected"))
    }

  }

}
