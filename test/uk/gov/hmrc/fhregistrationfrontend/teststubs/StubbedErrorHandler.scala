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

package uk.gov.hmrc.fhregistrationfrontend.teststubs

import play.api.mvc.{Request, Result, Results}
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler

object StubbedErrorHandler extends ErrorHandler with Results {

  override def errorResultsPages(errorResults: Results.Status, errorMsg: Option[String])(
    implicit request: Request[_]): Result =
    errorResults("")

  override def applicationError(implicit request: Request[_]): Result =
    Ok("fh.application_error.title")
}
