package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import uk.gov.hmrc.fhregistrationfrontend.models.des.PartnerType

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  def commonPrecondition = {
    builder
      .audit.writesAuditOrMerged()
      .user.isAuthorised()
      .businessCustomerFrontend.hasBusinessCustomerRecord
      .save4later.businessRecordWasSaved()
      .save4later.getNoneData()
      .fhddsBackend.hasNoEnrolmentProgress()
  }

  def commonPreconditionWithMultipleAddressLookup(isSuccess: Boolean) = {
    builder
      .audit.writesAuditOrMerged()
      .user.isAuthorised()
      .businessCustomerFrontend.hasBusinessCustomerRecord
      .save4later.businessRecordWasSaved()
      .save4later.getNoneData()
      .fhddsBackend.hasNoEnrolmentProgress()
      .addressLookup.lookupAddress(isSuccess)
  }

  def commonPreconditionWithSingleAddressLookup(isSuccess: Boolean) = {
    builder
      .audit.writesAuditOrMerged()
      .user.isAuthorised()
      .businessCustomerFrontend.hasBusinessCustomerRecord
      .save4later.businessRecordWasSaved()
      .save4later.getNoneData()
      .fhddsBackend.hasNoEnrolmentProgress()
      .addressLookup.lookupSingleAddress(isSuccess)
  }

  def commonPreconditionAssist = {
    builder
      .audit.writesAuditOrMerged()
      .user.isAuthorisedAssistant()
      .businessCustomerFrontend.hasBusinessCustomerRecord
      .save4later.businessRecordWasSaved()
      .save4later.getNoneData()
      .fhddsBackend.hasNoEnrolmentProgress()
  }

  def commonPreconditionNoRole = {
    builder
      .audit.writesAuditOrMerged()
      .user.isAuthorisedNoCredRole()
      .businessCustomerFrontend.hasBusinessCustomerRecord
      .save4later.businessRecordWasSaved()
      .save4later.getNoneData()
      .fhddsBackend.hasNoEnrolmentProgress()
  }

  def withdrawalPrecondition = {
    builder
      .audit.writesAuditOrMerged()
      .user.isAuthorisedAndEnrolled
  }

  def summaryPrecondition = {
    builder
      .audit.writesAuditOrMerged()
      .user.isAuthorised()
      .save4later.hasFullFormData()
  }

}