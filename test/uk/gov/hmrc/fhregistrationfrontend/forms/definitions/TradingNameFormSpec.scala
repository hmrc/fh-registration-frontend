/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatest.matchers.must.Matchers.must
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class TradingNameFormSpec extends UnitSpec {

  val form = TradingNameForm.tradingNameForm

  "TradingNameForm" should {

    "reject illegal characters in trading name" in {
      val result = form.bind(
        Map(
          TradingNameForm.hasTradingNameKey -> "true",
          TradingNameForm.tradingNameKey    -> "test{}"
        )
      )
      result.errors.map(_.message) must contain("error.pattern")
    }

    "reject missing trading name when selected" in {
      val result = form.bind(
        Map(
          TradingNameForm.hasTradingNameKey -> "true",
          TradingNameForm.tradingNameKey    -> ""
        )
      )
      result.errors.map(_.message) must contain("error.required")
    }
  }
}
