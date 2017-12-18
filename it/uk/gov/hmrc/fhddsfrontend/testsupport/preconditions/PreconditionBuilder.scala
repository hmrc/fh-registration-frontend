package uk.gov.hmrc.fhddsfrontend.testsupport.preconditions

class PreconditionBuilder {
  implicit val builder: PreconditionBuilder = this

  def audit = AuditStub()
  def user = UserStub()
  def businessCustomerFrontend = BusinessCustomerFrontendStub()
  def fhddsBackend = FhddsBackendStub()

}
