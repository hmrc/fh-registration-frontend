package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{EoriNumber, ImportingActivities}

trait MockHelper extends MockitoSugar {

  val mockAuditStub = mock[AuditStub]
  val mockUserStub = mock[UserStub]
  val mockKeyStoreStub = mock[KeyStoreStub]
  val mockFhddsBackendStub = mock[FhddsBackendStub]
  val mockBusinessCustomerFrontendStub = mock[BusinessCustomerFrontendStub]
  val mockSave4LaterStub = mock[Save4LaterStub]

  def setupCommonPreconditionMocks(): Unit = {
    when(mockAuditStub.writesAuditOrMerged()).thenReturn(mockAuditStub)
    when(mockUserStub.isAuthorised()).thenReturn(mockUserStub)
    when(mockBusinessCustomerFrontendStub.hasBusinessCustomerRecord).thenReturn(mockBusinessCustomerFrontendStub)
    when(mockSave4LaterStub.businessRecordWasSaved()).thenReturn(mockSave4LaterStub)
    when(mockSave4LaterStub.getNoneData()).thenReturn(mockSave4LaterStub)
    when(mockFhddsBackendStub.hasNoEnrolmentProgress()).thenReturn(mockFhddsBackendStub)
  }

  def setupSummaryPreconditionMocks(): Unit = {
    when(mockAuditStub.writesAuditOrMerged()).thenReturn(mockAuditStub)
    when(mockUserStub.isAuthorised()).thenReturn(mockUserStub)
    when(mockSave4LaterStub.hasFullFormData()).thenReturn(mockSave4LaterStub)

  }

  def setupCreateSubscriptionMocks(): Unit = {
    setupSummaryPreconditionMocks()
    when(mockFhddsBackendStub.createSubscription()).thenReturn(mockFhddsBackendStub)
  }

  def setupBusinessTypeWasSavedMocks(): Unit = {
    setupCommonPreconditionMocks()
    when(mockSave4LaterStub.businessTypeWasSaved()).thenReturn(mockSave4LaterStub)
  }

  def setupSavePageDataMocks(): Unit = {
    setupBusinessTypeWasSavedMocks()
    when(mockSave4LaterStub.savePageData("mainBusinessAddress", """{"timeAtCurrentAddress": "3-5 years"}"""))
      .thenReturn(mockSave4LaterStub)
  }

  def setupImportingActivitiesMocks(): Unit = {
    val importingActivitiesWithSplitFields =
      ImportingActivities(hasEori = true, eori = Some("1234123132"), goodsImported = Some(true))
    setupCommonPreconditionMocks()
    when(mockSave4LaterStub.hasFullFormDataWithImportingActivities(importingActivitiesWithSplitFields))
      .thenReturn(mockSave4LaterStub)
  }

  def setupWritesAuditOrMergedMocks(): Unit =
    when(mockAuditStub.writesAuditOrMerged()).thenReturn(mockAuditStub)

  def setupUserIsNotAuthorisedMocks(): Unit = {
    setupWritesAuditOrMergedMocks()
    when(mockUserStub.isNotAuthorised()).thenReturn(mockUserStub)
  }

  def setupUserIsAuthorisedMocks(): Unit = {
    setupWritesAuditOrMergedMocks()
    when(mockUserStub.isAuthorised()).thenReturn(mockUserStub)
  }

  def setupHasNoEnrolmentProgressMocks(): Unit =
    when(mockFhddsBackendStub.hasNoEnrolmentProgress()).thenReturn(mockFhddsBackendStub)

  def setupAuthorisedAssistantMocks(): Unit =
    when(mockUserStub.isAuthorisedAssistant()).thenReturn(mockUserStub)

  def setupIsAuthorisedNoCredRoleMocks(): Unit =
    when(mockUserStub.isAuthorisedNoCredRole()).thenReturn(mockUserStub)

  def setupIsAuthorisedAndEnrolledMocks(): Unit =
    when(mockUserStub.isAuthorisedAndEnrolled).thenReturn(mockUserStub)

  def setupHasBusinessCustomerRecordMocks(): Unit =
    when(mockBusinessCustomerFrontendStub.hasBusinessCustomerRecord).thenReturn(mockBusinessCustomerFrontendStub)

  def setupBusinessRecordWasSavedMocks(): Unit =
    when(mockSave4LaterStub.businessRecordWasSaved()).thenReturn(mockSave4LaterStub)

  def setupGetNoneDataMocks(): Unit =
    when(mockSave4LaterStub.getNoneData()).thenReturn(mockSave4LaterStub)

  def setupAcceptsDeleteMocks(): Unit =
    when(mockSave4LaterStub.acceptsDelete()).thenReturn(mockSave4LaterStub)

  def setupHasAmendmentDataWithNewVerifiedEmailMocks(): Unit =
    when(mockSave4LaterStub.hasAmendmentDataWithNewVerifiedEmail("a@test.com")).thenReturn(mockSave4LaterStub)

  def setupHasBusinessInformationWOVerifiedEmailMocks(): Unit =
    when(mockSave4LaterStub.hasBusinessInformationWOVerifiedEmail).thenReturn(mockSave4LaterStub)

  def setupHasFullPreEmailVerificationDataMocks(): Unit =
    when(mockSave4LaterStub.hasFullPreEmailVerificationData()).thenReturn(mockSave4LaterStub)

  def setupAcceptsAmendmentsMocks(): Unit =
    when(mockFhddsBackendStub.acceptsAmendments()).thenReturn(mockFhddsBackendStub)

  def setupGetSubscriptionMocks(): Unit =
    when(mockFhddsBackendStub.getSubscription()).thenReturn(mockFhddsBackendStub)

  def setupSaveWithdrawalReasonMocks(sessionId: String): Unit =
    when(mockKeyStoreStub.saveWithdrawalReason(sessionId)).thenReturn(mockKeyStoreStub)

}
