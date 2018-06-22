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

package uk.gov.hmrc.fhregistrationfrontend.teststubs

import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import play.api.mvc.{ActionBuilder, Request, Result}
import uk.gov.hmrc.fhregistrationfrontend.actions._
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{JourneyPages, Journeys}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

trait ActionsMock extends MockitoSugar with UserTestData {
  this: UnitSpec ⇒
  val mockActions = mock[Actions]

  def setupNewApplicationAction(): Unit = {
    when(mockActions.newApplicationAction) thenReturn new ActionBuilder[UserRequest] {
      override def invokeBlock[A](request: Request[A], block: UserRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), None, request)
        block(userRequest)
      }
    }
  }

  def setupJourneAction(rNumber: Option[String] = Some(registrationNumber), journeyPages: JourneyPages = new JourneyPages(Journeys.partnershipPages)) = {
    when(mockActions.journeyAction) thenReturn new  ActionBuilder[JourneyRequest] {
      override def invokeBlock[A](request: Request[A], block: JourneyRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), rNumber, request)
        val journeyRequest = JourneyRequestBuilder.journeyRequest(userRequest, journeyPages).asInstanceOf[JourneyRequest[A]]
        block(journeyRequest)
      }
    }
  }

  def setupEnrolledUserAction(registrationNumber: String = registrationNumber): Unit = {
    when(mockActions.enrolledUserAction) thenReturn new  ActionBuilder[EnrolledUserRequest] {
      override def invokeBlock[A](request: Request[A], block: EnrolledUserRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), Some(registrationNumber), request)
        val enrolledUserRequest = new EnrolledUserRequest(registrationNumber, userRequest)
        block(enrolledUserRequest)
      }
    }
  }

  def setupUserAction(rNumber: Option[String] = Some(registrationNumber)) = {
    when(mockActions.userAction) thenReturn new ActionBuilder[UserRequest] {
      override def invokeBlock[A](request: Request[A], block: UserRequest[A] ⇒ Future[Result]): Future[Result] = {
        val userRequest = new UserRequest(testUserId, Some(ggEmail), rNumber, request)
        block(userRequest)
      }
    }
  }
}
