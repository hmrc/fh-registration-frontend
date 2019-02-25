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

package uk.gov.hmrc.fhregistrationfrontend.config

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import play.api.Configuration
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Request, Result, Results}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.http.FrontendErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.views.html.{application_error, error_template}
import play.api.mvc.Results.Status

@ImplementedBy(classOf[DefaultErrorHandler])
trait ErrorHandler {
  def errorResultsPages(errorResults: Status, errorMsg: Option[String] = None)(implicit request: Request[_]): Result

  def applicationError(implicit request: Request[_]): Result
}

@Singleton
class DefaultErrorHandler @Inject()(
  val messagesApi: MessagesApi, val configuration: Configuration)(implicit val appConfig: AppConfig)
  extends FrontendErrorHandler with ErrorHandler {

  import Results._

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit rh: Request[_]): Html =
    error_template(pageTitle, heading, message)

  override def applicationError(implicit request: Request[_]): Result = {
    Ok(application_error())
  }

  override def errorResultsPages(errorResults: Status, errorMsg: Option[String] = None)(implicit request: Request[_]): Result = {
    val messages = implicitly[Messages]
    errorResults match {
      case NotFound ⇒ NotFound(error_template(
        messages("fh.generic.not_found"),
        messages("fh.generic.not_found.label"),
        errorMsg.getOrElse(messages("fh.generic.not_found.inf"))
      ))
      case BadRequest ⇒ BadRequest(error_template(
        messages("fh.generic.bad_request"),
        messages("fh.generic.bad_request.label"),
        errorMsg.getOrElse(messages("fh.generic.bad_request.inf"))
      ))
      case Unauthorized ⇒ Unauthorized(error_template(
        messages("fh.generic.unauthorized"),
        messages("fh.generic.unauthorized.label"),
        errorMsg.getOrElse(messages("fh.generic.unauthorized.inf"))
      ))
      case BadGateway ⇒ BadGateway(error_template(
        messages("fh.generic.bad_gateway"),
        messages("fh.generic.bad_gateway.label"),
        errorMsg.getOrElse(messages("fh.generic.bad_gateway.inf"))
      ))
      case _ ⇒ InternalServerError(error_template(
        messages("fh.generic.internal_server_error"),
        messages("fh.generic.internal_server_error.label"),
        errorMsg.getOrElse(messages("fh.generic.internal_server_error.inf"))
      ))
    }

  }

}
