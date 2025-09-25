package uk.gov.hmrc.fhregistrationfrontend.controllers.admin

import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.views.html._

/**
 * Test-only replacement for the giant Views class.
 * Uses named arguments to avoid parameter ordering issues.
 */
class TestViews(
                 override val temp_admin_page: uk.gov.hmrc.fhregistrationfrontend.views.html.temp_admin_page,
                 override val show_all_submissions: uk.gov.hmrc.fhregistrationfrontend.views.html.show_all_submissions,
                 override val show_submission: uk.gov.hmrc.fhregistrationfrontend.views.html.show_submission,
                 override val admin_get_groupID: uk.gov.hmrc.fhregistrationfrontend.views.html.admin_get_groupID,
                 override val allocate_enrolment: uk.gov.hmrc.fhregistrationfrontend.views.html.allocate_enrolment,
                 override val delete_enrolment: uk.gov.hmrc.fhregistrationfrontend.views.html.delete_enrolment
               ) extends Views(
  deregistration_acknowledgement = null,
  deregistration_confirm = null,
  deregistration_reason = null,
  email_change_start = null,
  email_edit = null,
  email_options = null,
  email_pending_verification = null,
  email_verified = null,
  business_customers = null,
  business_partners = null,
  business_status = null,
  company_officers = null,
  company_registration_number = null,
  contact_person = null,
  date_of_incorporation = null,
  has_other_storage_premises = null,
  importing_activities = null,
  has_eori = null,
  importing_activities_eori_yes_eori_number = null,
  importing_activities_eori_yes_goods = null,
  main_business_address = null,
  main_business_address_years_at_address = null,
  main_business_address_has_previous_address = null,
  main_business_address_previous_address = null,
  national_insurance_number = null,
  other_storage_premises = null,
  storage_premise = null,
  vat_registration = null,
  trading_name = null,
  withdrawal_acknowledgement = null,
  withdrawal_reason = null,
  withdrawal_confirm = null,
  confirm_delete = null,
  confirm_delete_section = null,
  continue_delete = null,
  delete_enrolment = delete_enrolment,
  allocate_enrolment = allocate_enrolment,
  declaration = null,
  enrolment_pending = null,
  error_forbidden = null,
  error_template = null,
  saved = null,
  ltd_summary = null,
  sole_proprietor_summary = null,
  partnership_summary = null,
  acknowledgement_page = null,
  admin_get_groupID = admin_get_groupID,
  application_error = null,
  business_type = null,
  temp_admin_page = temp_admin_page,
  show_all_submissions = show_all_submissions,
  show_submission = show_submission,
  contact_person_page = null,
  trading_name_page = null,
  business_status_page = null,
  other_storage_premises_page = null
)