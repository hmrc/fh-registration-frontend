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

import org.joda.time.LocalDate
import play.api.libs.json.Json

object FhddsModels {
  implicit val addressFormatter = Json.format[Address]
  implicit val officerFormatter = Json.format[Officer]
  implicit val companyFormatter = Json.format[Company]
  implicit val companyDetailsFormatter = Json.format[CompanyDetails]
}

case class Address(
  postalCode: String,
  addressLine1: Option[String] = None,
  addressLine2: Option[String] = None,
  premises: Option[String] = None,
  locality: Option[String] = None,
  region: Option[String] = None,
  country: Option[String] = None
)

case class Company(
  title: String,
  companyNumber: Option[String] = None,
  companyType  : Option[String] = None,
  companyStatus: Option[String] = None,
  director     : Option[Officer] = None

)

case class Officer(
  firstName  : String,
  lastName   : String,
  resignedOn : Option[LocalDate],
  officerRole: String) {

}


case class CompanyDetails(address: Option[Address], company: Option[Company])