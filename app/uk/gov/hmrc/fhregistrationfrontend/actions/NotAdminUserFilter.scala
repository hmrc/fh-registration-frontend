/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.mvc.{ActionFilter, Result, Results}
import uk.gov.hmrc.auth.core.{Assistant, User}
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler

import scala.concurrent.{ExecutionContext, Future}

class NotAdminUserFilter()(implicit val errorHandler: ErrorHandler, val executionContext: ExecutionContext)
    extends ActionFilter[UserRequest] with FrontendAction {

  override protected def filter[A](request: UserRequest[A]): Future[Option[Result]] = {
    implicit val r = request

    request.credentialRole match {
      case Some(credRole) if credRole == User || credRole == User => Future.successful(None)
      case Some(credRole) if credRole == Assistant =>
        Future.successful(Some(errorHandler.errorResultsPages(Results.Forbidden)))
      case _ ⇒ Future.successful(Some(errorHandler.errorResultsPages(Results.BadRequest)))
    }
  }
}
