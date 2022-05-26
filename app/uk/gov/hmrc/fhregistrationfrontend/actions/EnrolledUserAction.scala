/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.actions

import play.api.mvc.{ActionRefiner, Result, Results, WrappedRequest}
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler

import scala.concurrent.{ExecutionContext, Future}

class EnrolledUserRequest[A](
  val registrationNumber: String,
  val request: UserRequest[A]
) extends WrappedRequest[A](request) {

  def userId: String = request.userId
}

class EnrolledUserAction(implicit errorHandler: ErrorHandler, val executionContext: ExecutionContext)
    extends ActionRefiner[UserRequest, EnrolledUserRequest] with FrontendAction {

  override protected def refine[A](request: UserRequest[A]): Future[Either[Result, EnrolledUserRequest[A]]] = {
    implicit val r = request
    Future successful {
      request.registrationNumber match {
        case Some(registrationNumber) ⇒
          Right(new EnrolledUserRequest[A](registrationNumber, request))
        case None ⇒
          logger.error(s"Not found: registration number. Is user enrolled?")
          Left(errorHandler.errorResultsPages(Results.BadRequest))
      }
    }
  }
}
