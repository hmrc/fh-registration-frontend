package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import org.scalatestplus.mockito.MockitoSugar.mock

class PreconditionBuilder extends PreconditionHelpers {
  implicit val builder: PreconditionBuilder = this

  def audit = AuditStub()
  def user = UserStub()
  def businessCustomerFrontend = BusinessCustomerFrontendStub()
  def fhddsBackend = FhddsBackendStub()
  def save4later = Save4LaterStub()
  def keyStore = KeyStoreStub()

  def addressLookup = AddressLookupStub()
//
//  val mockAuditStub: AuditStub = mock[AuditStub]
//  val mockUserStub: UserStub = mock[UserStub]
//  val mockBusinessCustomerFrontendStub: BusinessCustomerFrontendStub = mock[BusinessCustomerFrontendStub]
//  val mockFhddsBackendStub: FhddsBackendStub = mock[FhddsBackendStub]
//  val mockSave4LaterStub: Save4LaterStub = mock[Save4LaterStub]
//  val mockKeyStoreStub: KeyStoreStub = mock[KeyStoreStub]
//  val mockAddressLookupStub: AddressLookupStub = mock[AddressLookupStub]
//
//  // Return mocks instead of real instances
//  def audit: AuditStub = mockAuditStub
//
//  def user: UserStub = mockUserStub
//
//  def businessCustomerFrontend: BusinessCustomerFrontendStub = mockBusinessCustomerFrontendStub
//
//  def fhddsBackend: FhddsBackendStub = mockFhddsBackendStub
//
//  def save4later: Save4LaterStub = mockSave4LaterStub
//
//  def keyStore: KeyStoreStub = mockKeyStoreStub
//
//  def addressLookup: AddressLookupStub = mockAddressLookupStub

}
