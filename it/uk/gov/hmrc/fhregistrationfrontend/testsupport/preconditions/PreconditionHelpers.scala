package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

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