/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.fhddsfrontend.models

import com.github.tototoshi.play.json.JsonNaming
import play.api.libs.json._


object CompaniesHouse {
  implicit val companyAddressReader = JsonNaming snakecase Json.reads[CompanyAddress]
  implicit val companyReader = JsonNaming snakecase Json.reads[Company]
  implicit val companySearchResultReader = JsonNaming snakecase Json.reads[CompanySearchResult]

}

case class Company(
  title: String,
  companyNumber: String,
  companyType: String,
  companyStatus: String,
  address: CompanyAddress

)


case class CompanyAddress(
  postalCode  : String,
  addressLine1: Option[String] = None,
  addressLine2: Option[String] = None,
  addressLine3: Option[String] = None,
  addressLine4: Option[String] = None,
  premises    : Option[String] = None,
  locality    : Option[String] = None,
  region      : Option[String] = None,
  country     : Option[String] = None
)

case class CompanySearchResult(items: List[Company])
