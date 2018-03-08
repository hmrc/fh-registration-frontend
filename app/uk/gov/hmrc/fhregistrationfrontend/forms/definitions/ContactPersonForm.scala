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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{personName,
  email, roleInOrganization, telephone, yesOrNo, address, internationalAddress}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl.MappingsApi.{MappingOps, MappingWithKeyOps}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.ContactPerson

object ContactPersonForm {

  val firstNameKey = "firstName"
  val lastNameKey = "lastName"
  val jobTitleKey = "jobTitle"
  val telephoneKey = "telephone"
  val emailAddressKey = "emailAddress"
  val usingSameContactAddressKey = "usingSameContactAddress"
  val isUkAddressKey = "isUkAddress"
  val otherUkContactAddressKey = "otherUkContactAddress_contactAddress"
  val otherInternationalContactAddressKey = "otherInternationalContactAddress_contactAddress"


  private val usingSameContactAddressMapping = usingSameContactAddressKey → yesOrNo
  private val ukAddressMapping = isUkAddressKey → (yesOrNo onlyWhen (usingSameContactAddressMapping is false))

  private val otherUkContactAddressMapping =
    otherUkContactAddressKey → (address onlyWhen (ukAddressMapping is Some(true)))

  private val otherInternationalContactAddressMapping =
    otherInternationalContactAddressKey → (internationalAddress onlyWhen (ukAddressMapping is Some(false)))

  val contactPersonForm = Form(
    mapping(
      firstNameKey → personName,
      lastNameKey → personName,
      jobTitleKey → roleInOrganization,
      telephoneKey → telephone,
      emailAddressKey → email,
      usingSameContactAddressMapping,
      ukAddressMapping,
      otherUkContactAddressMapping,
      otherInternationalContactAddressMapping
    )(ContactPerson.apply)(ContactPerson.unapply)
  )

}
