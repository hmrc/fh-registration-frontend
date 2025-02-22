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

package uk.gov.hmrc.fhregistrationfrontend.forms.models

import play.api.libs.json.{JsError, JsString, JsSuccess, Reads, Writes}

object CompanyOfficerType extends Enumeration {
  type CompanyOfficialType = Value

  val Individual, Company = Value

  // JSON Writes for CompanyOfficialType
  implicit val writes: Writes[CompanyOfficialType] = Writes { officialType =>
    JsString(officialType.toString)
  }

  // JSON Reads for CompanyOfficialType
  implicit val reads: Reads[CompanyOfficialType] = Reads {
    case JsString(s) =>
      values
        .find(_.toString == s)
        .map(JsSuccess(_))
        .getOrElse(JsError(s"Unknown CompanyOfficialType: $s"))
    case _ => JsError("String expected for CompanyOfficialType")
  }
}
