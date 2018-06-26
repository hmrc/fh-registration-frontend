/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import akka.stream.Materializer
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers.any
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.filters.csrf.CSRFAddToken
import uk.gov.hmrc.fhregistrationfrontend.actions.JourneyRequestBuilder
import uk.gov.hmrc.fhregistrationfrontend.connectors.BusinessCustomerFrontendConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessTypeForm
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.{EnrolmentProgress, FhddsStatus}
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterKeys
import uk.gov.hmrc.fhregistrationfrontend.teststubs._

import scala.concurrent.Future


class ApplicationControllerSpec
  extends ControllerSpecWithGuiceApp
    with FhddsConnectorMocks
    with ActionsMock
    with Save4LaterMocks
    with JourneyRequestBuilder
    with BeforeAndAfterEach
    with BeforeAndAfter
{

  val mockBusinessCustomerConnector = mock[BusinessCustomerFrontendConnector]

  override def beforeEach() = {
    super.beforeEach()
    reset(
      mockFhddsConnector,
      mockBusinessCustomerConnector,
      mockActions,
      mockSave4Later
    )

  }

  val controller = new Application(
    app.injector.instanceOf(classOf[ExternalUrls]),
    commonDependencies,
    mockFhddsConnector,
    mockBusinessCustomerConnector,
    mockActions
  )(mockSave4Later)

  "Application Controller" should {

    "Show status pending page" in {
      setupFhddsEnrolmentProgress(EnrolmentProgress.Pending)
      setupUserAction()
      val request = FakeRequest()
      val result = await(csrfAddToken(controller.main)(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages(s"fh.status.received.title"))
    }

    "Redirect to check status page" in {
      setupFhddsEnrolmentProgress(EnrolmentProgress.Unknown)
      setupUserAction()

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.main)(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/subscription/status")
    }
  }

  "Application Controller for an user with no registartion and no pending submissions" should {

    "Render the delete/continue form" in {
      setupFhddsEnrolmentProgress(EnrolmentProgress.Unknown)
      setupUserAction(rNumber = None)

      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.businessRegistrationDetailsKey, FormTestData.someBpr)
        .withValue(Save4LaterKeys.businessTypeKey, BusinessType.CorporateBody)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, System.currentTimeMillis())
        .cacheMap

      setupSave4LaterFrom(cacheMap)
      val request = FakeRequest()

      val result = await(csrfAddToken(controller.startOrContinueApplication)(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.continue_delete.header"))

    }

    "Redirect to check start page" in {
      setupFhddsEnrolmentProgress(EnrolmentProgress.Unknown)
      setupUserAction(rNumber = None)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.main)(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/start")
    }

    "Redirect to business customer service" in {
      setupFhddsEnrolmentProgress(EnrolmentProgress.Unknown)
      setupUserAction(rNumber = None)

      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.businessRegistrationDetailsKey, FormTestData.someBpr)
          .cacheMap

      setupSave4LaterFrom(cacheMap)
      val request = FakeRequest()

      val result = await(csrfAddToken(controller.startOrContinueApplication)(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("http://localhost:9923/business-customer/FHDDS")
    }
  }

  "submitDeleteOrContinue" should {
    def setupPreconditions(
      userLastTimeSaved: Option[Long] = None
    ) = {
      setupFhddsEnrolmentProgress(EnrolmentProgress.Unknown)
      setupUserAction(rNumber = None)
      val cacheMap = CacheMapBuilder(testUserId)
        .withOptValue(Save4LaterKeys.userLastTimeSavedKey, userLastTimeSaved)
        .cacheMap
      setupSave4LaterFrom(cacheMap)
    }

    "Fail if no answer was given" in {
      setupPreconditions(userLastTimeSaved = Some(System.currentTimeMillis()))
      val request = FakeRequest()
      val result = await(csrfAddToken(controller.submitDeleteOrContinue)(request))
      status(result) shouldBe BAD_REQUEST
    }

    "Fail when last update time is not present" in {
      setupPreconditions()
      val request = FakeRequest().withFormUrlEncodedBody(
        "deleteOrContinue" → "delete"
      )
      val result = await(csrfAddToken(controller.submitDeleteOrContinue)(request))
      status(result) shouldBe BAD_REQUEST
    }

    "Redirect to resumeForm when 'continue'" in {
      setupPreconditions(userLastTimeSaved = Some(System.currentTimeMillis()))

      val request = FakeRequest().withFormUrlEncodedBody(
        "deleteOrContinue" → "continue"
      )
      val result = await(csrfAddToken(controller.submitDeleteOrContinue)(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/resume")
    }

    "Render confirm_delete" in {
      setupPreconditions(userLastTimeSaved = Some(System.currentTimeMillis()))

      val request = FakeRequest().withFormUrlEncodedBody(
        "deleteOrContinue" → "delete"
      )
      val result = await(csrfAddToken(controller.submitDeleteOrContinue)(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.delete_confirmation_page.header"))
    }
  }

  "confirmDelete" should {
    def setup(userLastTimeSaved: Option[Long] = Some(System.currentTimeMillis())) = {
      setupFhddsEnrolmentProgress(EnrolmentProgress.Unknown)
      setupUserAction(rNumber = None)
      val cacheMap = CacheMapBuilder(testUserId)
        .withOptValue(Save4LaterKeys.userLastTimeSavedKey, userLastTimeSaved)
        .cacheMap
      setupSave4LaterFrom(cacheMap)
    }

    "Fail when no userLastTimeSaved is present" in {
      setup(userLastTimeSaved = None)
      val request = FakeRequest()
      val result = await(csrfAddToken(controller.confirmDelete)(request))

      status(result) shouldBe BAD_REQUEST
    }

    "Render the confirm_delete page" in {
      setup(userLastTimeSaved = Some(System.currentTimeMillis()))
      val request = FakeRequest()
      val result = await(csrfAddToken(controller.confirmDelete)(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.delete_confirmation_page.header"))
    }
  }

  "resumeForm" should {
    "Redirect to first page" in {
      setupJourneAction(rNumber = None)
      val request = FakeRequest()
      val result = await(controller resumeForm request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/form/contactPerson")
    }

    "Redirect to last completed page" in {
      setupJourneAction(rNumber = None, JourneyRequestBuilder.partiallyCompleteJourney)
      val request = FakeRequest()
      val result = await(controller resumeForm request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/form/mainBusinessAddress")
    }

    "Redirect to summary page" in {
      setupJourneAction(rNumber = None, JourneyRequestBuilder.fullyCompleteJourney())
      val request = FakeRequest()
      val result = await(controller resumeForm request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }

    "Redirect to last completed page with section" in {
      setupJourneAction(rNumber = None, JourneyRequestBuilder.partialJourneyWithSection)
      val request = FakeRequest()
      val result = await(controller resumeForm request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/form/businessPartners/6")
    }
  }

  "deleteUserData" should {
    "Redirect to startOrContinueApplication" in {
      setupUserAction()
      setupSave4LaterFrom(CacheMapBuilder(testUserId).cacheMap)

      val request = FakeRequest()
      val result = await(controller deleteUserData request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/start")
    }
  }

  "savedForLater" should {
    "Fail if no last time saved" in {
      setupUserAction()
      setupSave4LaterFrom(CacheMapBuilder(testUserId).cacheMap)

      val request = FakeRequest()
      val result = await(controller savedForLater request)

      status(result) shouldBe NOT_FOUND
    }

    "Render the saved page" in {
      setupUserAction()
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, System.currentTimeMillis())
        .cacheMap
      setupSave4LaterFrom(cacheMap)

      val request = FakeRequest()
      val result = await(controller savedForLater request)

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.has_saved"))
    }

  }

  "deleteOrContinue" should {
    "Redirect to businessType" in {
      setupUserAction()

      val request = FakeRequest()
      val result = await(controller.deleteOrContinue(true)(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/businessType")
    }

    "Redirect to businessType when no saved data" in {
      setupUserAction()
      setupSave4LaterFrom(CacheMapBuilder(testUserId).cacheMap)

      val request = FakeRequest()
      val result = await(controller.deleteOrContinue(false)(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/businessType")
    }


    "Render the continue_delete page" in {
      setupUserAction()
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, System.currentTimeMillis())
        .cacheMap
      setupSave4LaterFrom(cacheMap)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.deleteOrContinue(false))(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.continue_delete.header"))
    }

  }

  "continueWithBpr" should {
    "Redirect to the businessType page" in {
      setupNewApplicationAction()
      setupSave4LaterFrom(CacheMapBuilder(testUserId).cacheMap)
      when(mockBusinessCustomerConnector.getReviewDetails(any())) thenReturn Future.successful(FormTestData.someBpr)

      val request = FakeRequest()
      val result = await(controller continueWithBpr request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/businessType")
    }
  }

  "businessType" should {
    "Render the business_type page" in {
      setupUserAction()

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.businessType)(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.business_type.title"))
    }
  }

  "submitBusinessType" should {
    "Fail if the businessType is not given" in {
      setupUserAction()

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.submitBusinessType)(request))

      status(result) shouldBe BAD_REQUEST
      bodyOf(result) should include(Messages("fh.business_type.title"))
    }

    "Redirect to contactEmail" in {
      setupUserAction()
      setupSave4LaterFrom(CacheMapBuilder(testUserId).cacheMap)

      val request = FakeRequest().withFormUrlEncodedBody(
        BusinessTypeForm.businessTypeKey → BusinessType.CorporateBody.toString
      )

      val result = await(csrfAddToken(controller.submitBusinessType)(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/contact-email")
    }
  }

  "startForm" should {
    "Redirect to resumeForm" in {
      setupUserAction()

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.startForm)(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/resume")
    }
  }

  "checkStatus" should {
    "Render the status page" in {
      setupEnrolledUserAction()
      setupFhddsStatus(FhddsStatus.Deregistered)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.checkStatus())(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages(s"fh.status.${FhddsStatus.Deregistered.toString}.title"))
    }
  }

}
