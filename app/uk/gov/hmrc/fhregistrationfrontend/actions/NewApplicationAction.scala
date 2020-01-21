/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.mvc.{ActionRefiner, Result, Results}
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector

import scala.concurrent.{ExecutionContext, Future}

class NewApplicationAction(val fhddsConnector: FhddsConnector)(
  implicit val errorHandler: ErrorHandler,
  override val executionContext: ExecutionContext)
    extends ActionRefiner[UserRequest, UserRequest] with FrontendAction {

  override protected def refine[A](request: UserRequest[A]): Future[Either[Result, UserRequest[A]]] = {

    implicit val r = request

    import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus._

    val whenRegistered = request.registrationNumber.map { registrationNumber ⇒
      fhddsConnector.getStatus(registrationNumber).map {
        case Withdrawn | Rejected | Revoked | Deregistered ⇒ Right(request)
        case _ ⇒ Left(errorHandler.errorResultsPages(Results.BadRequest))
      }
    }

    whenRegistered getOrElse Future.successful(Right(request))
  }
}
