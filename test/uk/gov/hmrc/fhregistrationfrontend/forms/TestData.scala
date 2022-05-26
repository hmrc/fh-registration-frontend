/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.forms

import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address

object TestData {

  private val addressData = Map(
    "Line1" → "line one",
    "Line2" → "line two",
    "postcode" → "AA11 1AA"
  )

  def addressDataUk(prefix: String) =
    addressData map {
      case (k, v) ⇒
        s"$prefix.$k" -> v
    }

  val addressUk = Address("line one", Some("line two"), None, None, "AA11 1AA", None, None)

}
