/*
 * Copyright 2026 HM Revenue & Customs
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

import models.UserAnswers
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import play.api.Environment
import play.api.i18n.{DefaultMessagesApi, MessagesApi}
import play.api.mvc.{ActionBuilder, AnyContent, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{BAD_REQUEST, POST, contentAsString, defaultAwaitTimeout, status, stubMessagesControllerComponents}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.{AffinityGroup, User}
import uk.gov.hmrc.fhregistrationfrontend.actions.{Actions, JourneyRequest, SummaryRequest, UserRequest}
import uk.gov.hmrc.fhregistrationfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.DeclarationForm.{defaultEmailKey, fullNameKey, jobTitleKey, usingDefaultEmailKey}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{CachedJourneyState, JourneyPages, JourneyType, Journeys, Page}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.SubmissionOutcome.ActiveSubscription
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.DesToForm
import uk.gov.hmrc.fhregistrationfrontend.services.{Save4LaterService, SummaryConfirmationLocalService}
import uk.gov.hmrc.fhregistrationfrontend.teststubs.FormTestData
import uk.gov.hmrc.fhregistrationfrontend.views.{ViewHelpers, Views}

import scala.concurrent.{ExecutionContext, Future}

class DeclarationControllerSpec extends PlaySpec with MockitoSugar with ScalaFutures {
  private implicit val ec: ExecutionContext = ExecutionContext.global
  private implicit val save4LaterService: Save4LaterService = mock[Save4LaterService]

  private val mcc = stubMessagesControllerComponents()
  private val messagesApi: MessagesApi = mcc.messagesApi
  private val mockViews = mock[Views]
  private val mockDeclarationView = mock[uk.gov.hmrc.fhregistrationfrontend.views.html.declaration]
  private val mockActions = mock[Actions]
  private val mockDesToForm = mock[DesToForm]
  private val mockFhddsConnector = mock[FhddsConnector]
  private val mockSummaryConfirmationLocalService = mock[SummaryConfirmationLocalService]
  private val activeSubscriptionMessage =
    "This business already has an active Fulfilment House Due Diligence Scheme registration. You cannot submit another application."
  private val controllerMessagesApi: MessagesApi = new DefaultMessagesApi(
    Map("en" -> Map("fh.declaration.activeSubscription.error" -> activeSubscriptionMessage))
  )

  private val commonDependencies = new CommonPlayDependencies(
    Configuration.empty,
    mock[AppConfig],
    Environment.simple(),
    controllerMessagesApi,
    mock[ErrorHandler],
    mock[ViewHelpers]
  )

  private val journeys = new Journeys(mockViews)
  private val controller = new DeclarationController(
    commonDependencies,
    mockDesToForm,
    mockFhddsConnector,
    mockSummaryConfirmationLocalService,
    mcc,
    mockActions,
    journeys,
    mockViews
  )

  private val page = new Page.InjectedPage(mockViews)
  private val completeJourneyPages = new JourneyPages(
    Seq(
      page.contactPersonPage `withData` FormTestData.contactPerson,
      page.mainBusinessAddressPage `withData` FormTestData.mainBusinessAddress,
      page.tradingNamePage `withData` FormTestData.tradingName,
      page.vatNumberPage `withData` FormTestData.vatNumber,
      page.businessPartnersPage `withData` FormTestData.partners,
      page.businessStatusPage `withData` FormTestData.businessStatus,
      page.importingActivitiesPage `withData` FormTestData.importingActivities,
      page.businessCustomersPage `withData` FormTestData.businessCustomers,
      page.otherStoragePremisesPage `withData` FormTestData.otherStoragePremises
    )
  )

  when(mockViews.declaration).thenReturn(mockDeclarationView)
  when(mockDeclarationView.apply(any(), any(), any(), any(), any())(using any(), any()))
    .thenAnswer { invocation =>
      val alertMessage = invocation.getArgument[Option[String]](4).getOrElse("missing alert")
      HtmlFormat.raw(alertMessage)
    }

  when(mockActions.summaryAction).thenReturn(new ActionBuilder[SummaryRequest, AnyContent] {
    override def parser = mcc.parsers.defaultBodyParser
    override val executionContext: ExecutionContext = ec

    override def invokeBlock[A](request: Request[A], block: SummaryRequest[A] => Future[Result]): Future[Result] = {
      val userRequest = new UserRequest(
        userId = "Int-uid",
        ggEmail = Some("gg@test.com"),
        registrationNumber = None,
        credentialRole = Some(User),
        userAffinityGroup = Some(AffinityGroup.Individual),
        request
      )

      val journeyRequest = new JourneyRequest(
        UserAnswers("Int-uid"),
        userRequest,
        FormTestData.someBpr,
        BusinessType.Partnership,
        "gg@test.com",
        JourneyType.New,
        completeJourneyPages,
        new CachedJourneyState(completeJourneyPages)
      )

      block(new SummaryRequest(journeyRequest))
    }
  })

  "submitForm" should {
    "re-render the declaration page when create submission returns ACTIVE_SUBSCRIPTION" in {
      when(
        mockFhddsConnector.createSubmission(eqTo(FormTestData.someBpr.safeId.get), eqTo(None: Option[String]), any())(
          using any()
        )
      ).thenReturn(
        Future.successful(Left(ActiveSubscription))
      )

      val request = FakeRequest(POST, "/submit-declaration")
        .withFormUrlEncodedBody(
          fullNameKey          -> "Jane Example",
          jobTitleKey          -> "Director",
          usingDefaultEmailKey -> "true",
          defaultEmailKey      -> "gg@test.com"
        )

      val result = controller.submitForm()(request)

      status(result).mustBe(BAD_REQUEST)
      contentAsString(result).must(include(activeSubscriptionMessage))
    }
  }
}
