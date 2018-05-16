package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

class PreconditionBuilder extends PreconditionHelpers {
  implicit val builder: PreconditionBuilder = this

  def audit = AuditStub()
  def user = UserStub()
  def businessCustomerFrontend = BusinessCustomerFrontendStub()
  def fhddsBackend = FhddsBackendStub()
  def save4later = Save4LaterStub()
  def keyStore = KeyStoreStub()

}
