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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{Mode, UserAnswers}
import pages.Page
import play.api.i18n.I18nSupport
import play.api.mvc.{AnyContent, Call, Request, Result, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions.UserRequest
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

trait ControllerHelper extends FrontendBaseController with I18nSupport {

  val sessionCache: SessionRepository
  val errorHandler: ErrorHandler
  implicit val ec: ExecutionContext

  private def updateUserAnswersFailureMessage(page: Page) = s"Failed to add ${page.toString} data to user answers"

  private def sessionRepo500ErrorMessage(page: Page): String =
    s"Failed to set value in session repository while attempting set on ${page.toString}"

  def updateUserAnswersAndSaveToCache(updatedAnswers: Try[UserAnswers], nextPage: Call, page: Page)(
    implicit request: UserRequest[AnyContent]): Future[Result] =
    updatedAnswers match {
      case Failure(_) =>
        Future.successful(
          errorHandler.errorResultsPages(Results.InternalServerError, Some(updateUserAnswersFailureMessage(page))))
      case Success(answers) =>
        sessionCache
          .set(answers)
          .map { _ =>
            Redirect(nextPage)
          }
          .recover {
            case _ =>
              errorHandler.errorResultsPages(Results.InternalServerError, Some(sessionRepo500ErrorMessage(page)))
          }
    }

}
