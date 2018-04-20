/*
 * Copyright 2018 HM Revenue & Customs
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

import javax.inject.Inject

import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.actions.UserAction
import uk.gov.hmrc.fhregistrationfrontend.services.{KeyStoreService, Save4LaterService}
import uk.gov.hmrc.http.BadRequestException

@Inject
class PdfDownloadController @Inject()(
  ds            : CommonPlayDependencies,
  keyStoreService      : KeyStoreService
)(implicit save4LaterService: Save4LaterService) extends AppController(ds){

  def downloadPrintable() = UserAction().async { implicit request ⇒
    keyStoreService.fetchSummaryForPrint().map {
      case Some(userSummary) =>
        Ok(Html(removeScriptTags(userSummary)))
      case _ => throw new BadRequestException("no user summary found")
    }
  }

  protected def removeScriptTags(html: String) = html.replaceAll("<script[\\s\\S]*?/script>", "")

}

