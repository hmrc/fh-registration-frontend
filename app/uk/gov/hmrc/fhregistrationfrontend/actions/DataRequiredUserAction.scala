/*
 * Copyright 2023 HM Revenue & Customs
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

import models.{Mode, UserAnswers}
import play.api.mvc.{ActionRefiner, Result, WrappedRequest}
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.controllers.routes

import scala.concurrent.{ExecutionContext, Future}

class DataRequiredRequest[A](
  val userAnswers: UserAnswers,
  val request: UserRequest[A]
) extends WrappedRequest[A](request) {
  def userId: String = request.userId
}

class DataRequiredAction(val executionContext: ExecutionContext, index: Int, mode: Mode)
    extends ActionRefiner[DataRetrievedActionRequest, DataRequiredRequest] with FrontendAction {

  override protected def refine[A](
    request: DataRetrievedActionRequest[A]): Future[Either[Result, DataRequiredRequest[A]]] = {
    implicit val r = request
    Future successful {
      r.optUserAnswers match {
        case Some(userAnswers) =>
          Right(new DataRequiredRequest[A](userAnswers, r.request))
        case None =>
          logger.error(s"Not found: user answers")
          Left(Redirect(routes.BusinessPartnersController.load(index, mode)))
      }
    }
  }
}
