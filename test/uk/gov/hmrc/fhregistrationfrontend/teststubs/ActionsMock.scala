/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.teststubs

import akka.stream.Materializer
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.{any, same}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc._
import play.api.test.Helpers
import uk.gov.hmrc.auth.core.{Admin, AffinityGroup, CredentialRole}
import uk.gov.hmrc.fhregistrationfrontend.actions._
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page.AnyPage
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{JourneyPages, JourneyType, Journeys}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType.BusinessType
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

import scala.concurrent.{ExecutionContext, Future}

trait ActionsMock extends MockitoSugar with UserTestData {
  this: UnitSpec ⇒
  val mockActions = mock[Actions]
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
  implicit private val materializer = mock[Materializer]

  def setupPageAction(
    page: AnyPage,
    rNumber: Option[String] = Some(registrationNumber),
    credRole: Option[CredentialRole] = Some(adminRole),
    userAffinityGroup : AffinityGroup = AffinityGroup.Individual,
    journeyPages: JourneyPages = new JourneyPages(Journeys.partnershipPages),
    cacheMap: CacheMap = CacheMapBuilder(testUserId).cacheMap
  ) = {

    val actionBuilder = new ActionBuilder[PageRequest, AnyContent] {
      override def parser = Helpers.stubPlayBodyParsers.defaultBodyParser
      override val executionContext = ec
      override def invokeBlock[A](request: Request[A], block: PageRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), rNumber, credRole, Some(userAffinityGroup), request)
        val journeyRequest = JourneyRequestBuilder.journeyRequest(
          userRequest,
          journeyPages,
          cacheMap = cacheMap).asInstanceOf[JourneyRequest[A]]
        val journeyNavigation = if (journeyRequest.journeyState.isComplete)
          Journeys.summaryJourney(journeyRequest.journeyPages)
        else
          Journeys.linearJourney(journeyRequest.journeyPages)

        val pageRequest = new PageRequest(
          journeyNavigation,
          page,
          journeyRequest
        )
        block(pageRequest)
      }
    }
    when(mockActions.pageAction(page.id)) thenReturn actionBuilder
    when(mockActions.pageAction(same(page.id), any())) thenReturn actionBuilder
  }

  def setupSummaryAction(
    rNumber: Option[String] = Some(registrationNumber),
    journeyPages: JourneyPages = new JourneyPages(Journeys.partnershipPages),
    credRole: Option[CredentialRole] = Some(adminRole),
    userAffinityGroup : AffinityGroup = AffinityGroup.Individual,
    businessType: BusinessType = BusinessType.Partnership,
    journeyType: JourneyType = JourneyType.Amendment
  ) = {
    when(mockActions.summaryAction) thenReturn new  ActionBuilder[SummaryRequest, AnyContent] {
      override def parser = Helpers.stubPlayBodyParsers.defaultBodyParser
      override val executionContext = ec
      override def invokeBlock[A](request: Request[A], block: SummaryRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), rNumber, credRole, Some(userAffinityGroup), request)
        val journeyRequest = JourneyRequestBuilder.journeyRequest(userRequest, journeyPages, businessType, journeyType).asInstanceOf[JourneyRequest[A]]
        val summaryRequest = new SummaryRequest(journeyRequest)
        block(summaryRequest)
      }
    }
  }

  def setupNewApplicationAction(): Unit = {
    when(mockActions.newApplicationAction) thenReturn new ActionBuilder[UserRequest, AnyContent] {
      override def parser = Helpers.stubPlayBodyParsers.defaultBodyParser
      override val executionContext = ec
      override def invokeBlock[A](request: Request[A], block: UserRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), None, Some(Admin), Some(userAffinityGroup), request)
        block(userRequest)
      }
    }
  }

  def setupStartAmendmentAction(currentJourneyType: Option[JourneyType]): Unit = {
    when(mockActions.startAmendmentAction) thenReturn new ActionBuilder[StartUpdateRequest, AnyContent] {
      override def parser = Helpers.stubPlayBodyParsers.defaultBodyParser
      override val executionContext = ec
      override def invokeBlock[A](request: Request[A], block: StartUpdateRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), Some(registrationNumber), Some(Admin), Some(userAffinityGroup), request)
        val updateRequest = new StartUpdateRequest(registrationNumber, currentJourneyType, userRequest)
        block(updateRequest)
      }
    }
  }

  def setupStartVariationAction(currentJourneyType: Option[JourneyType]): Unit = {
    when(mockActions.startVariationAction) thenReturn new ActionBuilder[StartUpdateRequest, AnyContent] {
      override def parser = Helpers.stubPlayBodyParsers.defaultBodyParser
      override val executionContext = ec
      override def invokeBlock[A](request: Request[A], block: StartUpdateRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), Some(registrationNumber), Some(Admin), Some(userAffinityGroup), request)
        val updateRequest = new StartUpdateRequest(registrationNumber, currentJourneyType, userRequest)
        block(updateRequest)
      }
    }
  }

  def setupEmailVerificationAction(verifiedEmail: Option[String], pendingEmail: Option[String]): Any = {
    val candidateEmail = verifiedEmail orElse pendingEmail orElse Some(ggEmail)
    when(mockActions.emailVerificationAction) thenReturn new ActionBuilder[EmailVerificationRequest, AnyContent] {
      override def parser = Helpers.stubPlayBodyParsers.defaultBodyParser
      override val executionContext = ec
      override def invokeBlock[A](request: Request[A], block: EmailVerificationRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), Some(registrationNumber), Some(Admin), Some(userAffinityGroup), request)
        val emailVerificationRequest = new EmailVerificationRequest(
          verifiedEmail, pendingEmail, candidateEmail, userRequest
        )
        block(emailVerificationRequest)
      }
    }

  }

  def setupJourneAction(rNumber: Option[String] = Some(registrationNumber), journeyPages: JourneyPages = new JourneyPages(Journeys.partnershipPages)) = {
    when(mockActions.journeyAction) thenReturn new  ActionBuilder[JourneyRequest, AnyContent] {
      override def parser = Helpers.stubPlayBodyParsers.defaultBodyParser
      override val executionContext = ec
      override def invokeBlock[A](request: Request[A], block: JourneyRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), rNumber, Some(Admin), Some(userAffinityGroup),  request)
        val journeyRequest = JourneyRequestBuilder.journeyRequest(userRequest, journeyPages).asInstanceOf[JourneyRequest[A]]
        block(journeyRequest)
      }
    }
  }

  def setupEnrolledUserAction(registrationNumber: String = registrationNumber): Unit = {
    when(mockActions.enrolledUserAction) thenReturn new  ActionBuilder[EnrolledUserRequest, AnyContent] {
      override def parser = Helpers.stubPlayBodyParsers.defaultBodyParser
      override val executionContext = ec
      override def invokeBlock[A](request: Request[A], block: EnrolledUserRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), Some(registrationNumber), Some(Admin), Some(AffinityGroup.Individual), request)
        val enrolledUserRequest = new EnrolledUserRequest(registrationNumber, userRequest)
        block(enrolledUserRequest)
      }
    }
  }

  def setupUserAction(rNumber: Option[String] = Some(registrationNumber), credentialRole: Option[CredentialRole] = Some(Admin), userAffinityGroup : AffinityGroup = AffinityGroup.Individual) = {
    when(mockActions.userAction) thenReturn new ActionBuilder[UserRequest, AnyContent] {
      override def parser = Helpers.stubPlayBodyParsers.defaultBodyParser
      override val executionContext = ec
      override def invokeBlock[A](request: Request[A], block: UserRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), rNumber, credentialRole,  Some(userAffinityGroup),request)
        block(userRequest)
      }
    }
  }
}
