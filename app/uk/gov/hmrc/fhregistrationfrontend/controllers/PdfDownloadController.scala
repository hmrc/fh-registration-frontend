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

import uk.gov.hmrc.fhregistrationfrontend.actions.UserAction
import uk.gov.hmrc.fhregistrationfrontend.connectors.PdfGeneratorConnector
import uk.gov.hmrc.fhregistrationfrontend.services.{KeyStoreService, Save4LaterService}
import uk.gov.hmrc.http.BadRequestException

@Inject
class PdfDownloadController @Inject()(
  ds            : CommonPlayDependencies,
  keyStoreService      : KeyStoreService,
  pdfGeneratorConnector: PdfGeneratorConnector
)(implicit save4LaterService: Save4LaterService) extends AppController(ds){

  def downloadPdf() = UserAction.async { implicit request ⇒
    keyStoreService.fetchAndGetEntry().flatMap {
      case Some(userSummary) =>
        pdfGeneratorConnector.generatePdf(removeScriptTags(userSummary)).map { response =>
          if (response.status != OK)
            BadRequest(response.body)
          else
            Ok(response.bodyAsBytes.toArray).as("application/pdf")
              .withHeaders("Content-Disposition" -> s"attachment; filename=application.pdf")
              .withHeaders("Content-Type" -> s"application/pdf")
              .withHeaders("Content-Length" → s"${response.header("Content-Length").getOrElse("unknown")}")
        } recover {
          case e: Exception => throw new BadRequestException(e.toString)
        }
      case _ => throw new BadRequestException("no user summary found")
    }
  }

  protected def removeScriptTags(html: String) = html.replaceAll("<script[\\s\\S]*?/script>", "")

}

