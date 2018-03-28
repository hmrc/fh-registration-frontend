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
      .businessCustomerFrontend.hasBusinessPartnerRecord
      .save4later.businessRecordHasSaved()
      .save4later.getNoneData()
  }

}