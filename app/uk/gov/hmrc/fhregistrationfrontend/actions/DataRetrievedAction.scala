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

import models.UserAnswers
import play.api.mvc.{ActionTransformer, WrappedRequest}
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievedActionRequest[A](
  val request: UserRequest[A],
  val optUserAnswers: Option[UserAnswers]
) extends WrappedRequest[A](request) {

  def userId: String = request.userId
}

class DataRetrievedAction(sessionCache: SessionRepository)(implicit val executionContext: ExecutionContext)
    extends ActionTransformer[UserRequest, DataRetrievedActionRequest] with FrontendAction {

  override protected def transform[A](request: UserRequest[A]): Future[DataRetrievedActionRequest[A]] = {
    implicit val r = request
    sessionCache.get(r.userId).map { optUserAnswers =>
      new DataRetrievedActionRequest[A](request, optUserAnswers)
    }
  }
}
