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

package uk.gov.hmrc.fhregistrationfrontend.views
import javax.inject.{Inject, Singleton}

@Singleton
class Views @Inject() (
  //////////////////////////// Deregistration///////////////////////////////////
  val deregistration_acknowledgement: uk.gov.hmrc.fhregistrationfrontend.views.html.deregistration.deregistration_acknowledgement,
  val deregistration_confirm: uk.gov.hmrc.fhregistrationfrontend.views.html.deregistration.deregistration_confirm,
  val deregistration_reason: uk.gov.hmrc.fhregistrationfrontend.views.html.deregistration.deregistration_reason,
  //////////////////////////// Email////////////////////////////////////////////
  val email_change_start: uk.gov.hmrc.fhregistrationfrontend.views.html.emailverification.email_change_start,
  val email_edit: uk.gov.hmrc.fhregistrationfrontend.views.html.emailverification.email_edit,
  val email_options: uk.gov.hmrc.fhregistrationfrontend.views.html.emailverification.email_options,
  val email_pending_verification: uk.gov.hmrc.fhregistrationfrontend.views.html.emailverification.email_pending_verification,
  val email_verified: uk.gov.hmrc.fhregistrationfrontend.views.html.emailverification.email_verified,
  //////////////////////////// Forms/////////////////////////////////////////////
  val business_customers: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.business_customers,
  val business_partners: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.business_partners,
  val business_status: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.business_status,
  val company_officers: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.company_officers,
  val company_registration_number: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.company_registration_number,
  val contact_person: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.contact_person,
  val date_of_incorporation: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.date_of_incorporation,
  val has_other_storage_premises: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.has_other_storage_premises,
  val importing_activities: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.importing_activities,
  val has_eori: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.has_eori,
  val importing_activities_eori_yes_eori_number: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.importing_activities_eori_yes_eori_number,
  val importing_activities_eori_yes_goods: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.importing_activities_eori_yes_goods,
  val main_business_address: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.main_business_address,
  val main_business_address_years_at_address: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.main_business_address_years_at_address,
  val main_business_address_has_previous_address: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.main_business_address_has_previous_address,
  val main_business_address_previous_address: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.main_business_address_previous_address,
  val national_insurance_number: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.national_insurance_number,
  val other_storage_premises: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.other_storage_premises,
  val storage_premise: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.storage_premise,
  val vat_registration: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.vat_registration,
  val trading_name: uk.gov.hmrc.fhregistrationfrontend.views.html.forms.trading_name,
  //////////////////////////// Withdrawals///////////////////////////////////////////
  val withdrawal_acknowledgement: uk.gov.hmrc.fhregistrationfrontend.views.html.withdrawals.withdrawal_acknowledgement,
  val withdrawal_reason: uk.gov.hmrc.fhregistrationfrontend.views.html.withdrawals.withdrawal_reason,
  val withdrawal_confirm: uk.gov.hmrc.fhregistrationfrontend.views.html.withdrawals.withdrawal_confirm,
  //////////////////////////// Delete///////////////////////////////////////////////
  val confirm_delete: uk.gov.hmrc.fhregistrationfrontend.views.html.confirm_delete,
  val confirm_delete_section: uk.gov.hmrc.fhregistrationfrontend.views.html.confirm_delete_section,
  val continue_delete: uk.gov.hmrc.fhregistrationfrontend.views.html.continue_delete,
  //////////////////////////// Enrollment///////////////////////////////////////////
  val delete_enrolment: uk.gov.hmrc.fhregistrationfrontend.views.html.delete_enrolment,
  val allocate_enrolment: uk.gov.hmrc.fhregistrationfrontend.views.html.allocate_enrolment,
  val declaration: uk.gov.hmrc.fhregistrationfrontend.views.html.declaration,
  val enrolment_pending: uk.gov.hmrc.fhregistrationfrontend.views.html.enrolment_pending,
  //////////////////////////// Miscellaneous/////////////////////////////////////////////////
  val error_forbidden: uk.gov.hmrc.fhregistrationfrontend.views.html.error_forbidden,
  val error_template: uk.gov.hmrc.fhregistrationfrontend.views.html.error_template,
  val saved: uk.gov.hmrc.fhregistrationfrontend.views.html.saved,
  val ltd_summary: uk.gov.hmrc.fhregistrationfrontend.views.html.ltd_summary,
  val sole_proprietor_summary: uk.gov.hmrc.fhregistrationfrontend.views.html.sole_proprietor_summary,
  val partnership_summary: uk.gov.hmrc.fhregistrationfrontend.views.html.partnership_summary,
  val acknowledgement_page: uk.gov.hmrc.fhregistrationfrontend.views.html.acknowledgement_page,
  val admin_get_groupID: uk.gov.hmrc.fhregistrationfrontend.views.html.admin_get_groupID,
  val application_error: uk.gov.hmrc.fhregistrationfrontend.views.html.application_error,
  val business_type: uk.gov.hmrc.fhregistrationfrontend.views.html.business_type,
  val temp_admin_page: uk.gov.hmrc.fhregistrationfrontend.views.html.temp_admin_page,
  val show_all_submissions: uk.gov.hmrc.fhregistrationfrontend.views.html.show_all_submissions,
  val show_submission: uk.gov.hmrc.fhregistrationfrontend.views.html.show_submission,
  val contact_person_page: uk.gov.hmrc.fhregistrationfrontend.views.html.contact_person,
  val trading_name_page: uk.gov.hmrc.fhregistrationfrontend.views.html.trading_name,
  val business_status_page: uk.gov.hmrc.fhregistrationfrontend.views.html.business_status,
  val other_storage_premises_page: uk.gov.hmrc.fhregistrationfrontend.views.html.other_storage_premises
)
