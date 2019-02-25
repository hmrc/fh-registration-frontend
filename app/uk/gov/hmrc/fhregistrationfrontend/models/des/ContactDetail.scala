/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.models.des

import play.api.libs.json.Json

case class RoleInOrganization(beneficialShareHolder: Boolean,
                              director: Boolean,
                              partner: Boolean,
                              internalAccountant: Boolean,
                              soleProprietor: Boolean,
                              nominatedOfficer: Boolean,
                              designatedmember: Boolean,
                              otherRoleType: Boolean,
                              otherRoleDescription: Option[String])

object RoleInOrganization {
  implicit val format = Json.format[RoleInOrganization]
  
  def otherRole(description: String) = RoleInOrganization(
    beneficialShareHolder = false,
    director = false,
    partner = false,
    internalAccountant = false,
    soleProprietor = false,
    nominatedOfficer = false,
    designatedmember = false,
    otherRoleType = true,
    otherRoleDescription = Some(description)
  )
  
}

case class CommonDetails(telephone: Option[String],
                         mobileNumber: Option[String],
                         email: Option[String])

object CommonDetails {
  implicit val format = Json.format[CommonDetails]
  def apply(): CommonDetails = CommonDetails(None, None, None)
}

case class ContactDetail(title: Option[String],
                         names: Name,
                         usingSameContactAddress: Boolean,
                         address: Option[Address],
                         commonDetails: CommonDetails,
                         roleInOrganization: Option[RoleInOrganization])

object ContactDetail {
  implicit val format = Json.format[ContactDetail]
}