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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Results}
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.services.SummaryConfirmationService

import javax.inject.Inject
import scala.concurrent.ExecutionContext

@Inject
class PdfDownloadController @Inject() (
  ds: CommonPlayDependencies,
  summaryConfirmationService: SummaryConfirmationService,
  cc: MessagesControllerComponents,
  actions: Actions
)(implicit ec: ExecutionContext)
    extends AppController(ds, cc) {

  import actions._
  def downloadPrintable(): Action[AnyContent] = userAction.async { implicit request =>
    summaryConfirmationService.fetchSummaryForPrint().map {
      case Some(userSummary) =>
        Ok(Html(removeScriptTags(userSummary)))
      case _ =>
        errorHandler.errorResultsPages(Results.BadRequest, None)

    }
  }

  protected def removeScriptTags(html: String) = html.replaceAll("<script[\\s\\S]*?/script>", "")
}
