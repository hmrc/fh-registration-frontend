/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.views

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.fhregistrationfrontend.views.html._
@Singleton
class Views @Inject()(
  val deregistration_acknowledgment: deregistration.deregistration_acknowledgement,
  val deregistration_confirm:        deregistration.deregistration_confirm,
  val deregistration_reason:         deregistration.deregistration_reason,
  val email_change_start:            emailverification.email_change_start,
  val email_edit:                    emailverification.email_edit,
  val email_options:                 emailverification.email_options,
  val email_pending_verification:    emailverification.email_pending_verification,
  val email_verified:                emailverification.email_verified,
  val business_customers:            forms.business_customers,
  val business_partners:             forms.business_partners,
  val business_status:               forms.business_status,
  val company_officers:              forms.company_officers,
  val company_registration_number:   forms.company_registration_number,
  val contact_person:                forms.contact_person,
  val date_of_incorporation:         forms.date_of_incorporation,
  val examples:                      forms.examples,
  val has_other_storage_premises:    forms.has_other_storage_premises,
  val importing_activities:          forms.importing_activities,
  val main_business_address:         forms.main_business_address,
  val national_insurance_number:     forms.national_insurance_number,
  val other_storage_premises:        forms.other_storage_premises,
  val storage_premise:               forms.trading_name,
  val vat_registration:              forms.vat_registration
)