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

import java.time.LocalDateTime

import javax.inject.Inject
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Request}
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.actions.{SummaryAction, SummaryRequest, UserAction}
import uk.gov.hmrc.fhregistrationfrontend.connectors.PdfGeneratorConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Journeys
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.services.{KeyStoreService, Save4LaterService}
import uk.gov.hmrc.fhregistrationfrontend.views.html.{ltd_summary, partnership_summary, sole_proprietor_summary}
import uk.gov.hmrc.http.BadRequestException


@Inject
class SummaryController @Inject()(
  ds                   : CommonPlayDependencies,
  pdfGeneratorConnector: PdfGeneratorConnector,
  links                : ExternalUrls,
  keyStoreService      : KeyStoreService
)(implicit save4LaterService: Save4LaterService, messages: Messages, request: Request[_]) extends AppController(ds) {

  def downloadPdf(timeStamp: String = LocalDateTime.now().toString) = UserAction.async { implicit request ⇒
    keyStoreService.fetchAndGetEntry().flatMap {
      case Some(userSummary) =>
        val summaryHtmlForPrint: String = userSummary.replace("timeStampPlaceHolder", timeStamp)
        pdfGeneratorConnector.generatePdf(removeScriptTags(summaryHtmlForPrint)).map { response =>
          if (response.status != OK)
            BadRequest(response.body)
          else
            Ok(response.bodyAsBytes.toArray).as("application/pdf")
              .withHeaders("Content-Disposition" -> s"attachment; filename=${request.userId}.pdf")
              .withHeaders("Content-Type" -> s"application/pdf")
              .withHeaders("Content-Length" → s"${response.header("Content-Length").getOrElse("unknown")}")
        } recover {
          case e: Exception => throw new BadRequestException(e.toString)
        }
      case _ => throw new BadRequestException("no user summary found")
    }
  }


  def summary() = SummaryAction(save4LaterService, messages, request).async { implicit request ⇒
    keyStoreService.save(getSummaryHtml(request, forPrint = true, timeStamp="timeStampPlaceHolder").toString()).map(
      _⇒ Ok(getSummaryHtml(request))
    )
  }

  private def getSummaryHtml(request: SummaryRequest[AnyContent], forPrint: Boolean = false, timeStamp: String = ""): Html = {

    val urlProtocol = ds.conf
      .getString(s"${ds.env.mode}.microservice.services.fhdds-front.protocol").getOrElse("http")
    val urlHost = ds.conf
      .getString(s"${ds.env.mode}.microservice.services.fhdds-front.host").getOrElse("fh-registration-frontend.public.mdtp")
    val urlPort = ds.conf
      .getInt(s"${ds.env.mode}.microservice.services.fhdds-front.port").getOrElse(80)

    val url = s"$urlProtocol://$urlHost:$urlPort"

    request.businessType match {
      case BusinessType.CorporateBody ⇒ ltdSummary(forPrint, url, timeStamp)(request)
      case BusinessType.SoleTrader ⇒ soleTrader(forPrint, url, timeStamp)(request)
      case BusinessType.Partnership ⇒ partnership(forPrint, url, timeStamp)(request)
    }
  }

  private def removeScriptTags(html: String) = html.replaceAll("<script[\\s\\S]*?/script>", "")

  def partnership(forPrint: Boolean = false, baseUrl: String, timeStamp: String = "")(implicit request: SummaryRequest[AnyContent]) = {
    val application = Journeys partnershipApplication request
    partnership_summary(application, request.bpr, baseUrl, forPrint, timeStamp)
  }


  def soleTrader(forPrint: Boolean = false, baseUrl: String, timeStamp: String = "")(implicit request: SummaryRequest[AnyContent]) = {
    val application = Journeys soleTraderApplication request
    sole_proprietor_summary(application, request.bpr, baseUrl, forPrint, timeStamp)
  }

  def ltdSummary(forPrint: Boolean = false, baseUrl: String, timeStamp: String = "")(implicit request: SummaryRequest[AnyContent]) = {
    val application = Journeys ltdApplication request

    ltd_summary(application, request.bpr, baseUrl, forPrint, timeStamp)
  }
}
