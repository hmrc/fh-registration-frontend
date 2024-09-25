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

package uk.gov.hmrc.fhregistrationfrontend.config

import javax.inject.{Inject, Singleton}
import com.google.inject.ImplementedBy
import play.api.Configuration
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Request, RequestHeader, Result, Results}
import play.twirl.api.Html
import play.api.mvc.Results.Status
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.play.bootstrap.frontend.http.FrontendErrorHandler

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[DefaultErrorHandler])
trait ErrorHandler {
  def errorResultsPages(errorResults: Status, errorMsg: Option[String] = None)(implicit request: Request[_]): Result

  def applicationError(implicit request: Request[_]): Result
}

@Singleton
class DefaultErrorHandler @Inject() (val messagesApi: MessagesApi, val configuration: Configuration, views: Views)(
  implicit val appConfig: AppConfig,
  implicit val ec: ExecutionContext
) extends FrontendErrorHandler with ErrorHandler {

  import Results._

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
    rh: RequestHeader
  ): Future[Html] =
    Future.successful(views.error_template(pageTitle, heading, message))

  override def applicationError(implicit request: Request[_]): Result =
    Ok(views.application_error())

  override def errorResultsPages(errorResults: Status, errorMsg: Option[String] = None)(implicit
    request: Request[_]
  ): Result = {
    val messages = implicitly[Messages]
    errorResults match {
      case NotFound =>
        NotFound(
          views.error_template(
            messages("fh.generic.not_found"),
            messages("fh.generic.not_found.label"),
            errorMsg.getOrElse(messages("fh.generic.not_found.inf"))
          )
        )
      case Forbidden =>
        Forbidden(
          views.error_forbidden(
            messages("fh.generic.forbidden"),
            messages("fh.generic.forbidden.label"),
            errorMsg.getOrElse(messages("fh.generic.forbidden.inf"))
          )
        )
      case BadRequest =>
        BadRequest(
          views.error_template(
            messages("fh.generic.bad_request"),
            messages("fh.generic.bad_request.label"),
            errorMsg.getOrElse(messages("fh.generic.bad_request.inf"))
          )
        )
      case Unauthorized =>
        Unauthorized(
          views.error_template(
            messages("fh.generic.unauthorized"),
            messages("fh.generic.unauthorized.label"),
            errorMsg.getOrElse(messages("fh.generic.unauthorized.inf"))
          )
        )
      case BadGateway =>
        BadGateway(
          views.error_template(
            messages("fh.generic.bad_gateway"),
            messages("fh.generic.bad_gateway.label"),
            errorMsg.getOrElse(messages("fh.generic.bad_gateway.inf"))
          )
        )
      case _ =>
        InternalServerError(
          views.error_template(
            messages("fh.generic.internal_server_error"),
            messages("fh.generic.internal_server_error.label"),
            errorMsg.getOrElse(messages("fh.generic.internal_server_error.inf"))
          )
        )
    }

  }

}
