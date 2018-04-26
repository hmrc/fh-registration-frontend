package uk.gov.hmrc.fhregistrationfrontend.testsupport

import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions.PreconditionBuilder

trait TestHelper
  extends PlaySpec with WordSpecLike with ScalaFutures {

  def given() = new PreconditionBuilder

  def commonPrecondition = {
    given()
      .audit.writesAuditOrMerged()
      .user.isAuthorised()
      .businessCustomerFrontend.hasBusinessCustomerRecord
      .save4later.businessRecordHasSaved()
      .save4later.getNoneData()
      .fhddsBackend.hasNoEnrolmentProgress()
  }

  def withdrawalPrecondition = {
    given()
      .audit.writesAuditOrMerged()
      .user.isAuthorisedAndEnrolled()
  }

  def summaryPrecondition = {
    given()
      .audit.writesAuditOrMerged()
      .user.isAuthorised()
      .save4later.getFullData()
  }

}