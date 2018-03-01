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
import play.api.mvc.AnyContent
import uk.gov.hmrc.fhregistrationfrontend.actions.{SummaryAction, SummaryRequest}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Journeys
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.actions.{SummaryAction, SummaryRequest, UserAction}
import uk.gov.hmrc.fhregistrationfrontend.connectors.PdfGeneratorConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessType, LimitedCompanyApplication, PartnershipApplication, SoleProprietorApplication}
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.views.html.ltd_summary
import uk.gov.hmrc.fhregistrationfrontend.views.html.sole_proprietor_summary
import uk.gov.hmrc.fhregistrationfrontend.views.html.partnership_summary
import uk.gov.hmrc.http.BadRequestException


@Inject
class SummaryController @Inject()(
  ds                   : CommonPlayDependencies,
  pdfGeneratorConnector: PdfGeneratorConnector,
  messagesApi          : play.api.i18n.MessagesApi,
  links                : ExternalUrls
)(implicit save4LaterService: Save4LaterService) extends AppController(ds, messagesApi) {

  def downloadPdf(timeStamp: String = LocalDateTime.now().toString) = SummaryAction(save4LaterService).async { implicit request ⇒
    val summaryHtml: Html = getSummaryHtml(request)
    pdfGeneratorConnector.generatePdf(removeScriptTags(summaryHtml.toString)).map { response =>
      if (response.status != OK)
        BadRequest(response.body)
      else
        Ok(response.body.getBytes).as("application/pdf")
          .withHeaders("Content-Disposition" -> s"attachment; filename=${request.userId}.pdf")
    } recover {
      case e: Exception => {
        throw new BadRequestException(e.toString)
      }
    }
  }


  def summary() = SummaryAction(save4LaterService) { implicit request ⇒
    Ok(getSummaryHtml(request))
  }

  private def getSummaryHtml(request: SummaryRequest[AnyContent]): Html = {
    implicit val environment = ds.env
    val urlProtocol = ds.conf
      .getString(s"${ds.env.mode}.microservice.services.fhdds-front.protocol").getOrElse("http")
    val urlHost = ds.conf
      .getString(s"${ds.env.mode}.microservice.services.fhdds-front.host")
      .getOrElse("fh-registration-frontend.public.mdtp")
    val urlPort = ds.conf
      .getInt(s"${ds.env.mode}.microservice.services.fhdds-front.port").getOrElse(80)
    val url = s"$urlProtocol://$urlHost:$urlPort"
    println(s"\n\n$url\n\n")
    request.businessType match {
      case BusinessType.CorporateBody ⇒ ltdSummary(request, url)
      case BusinessType.SoleTrader ⇒ soleTrader(request, url)
      case BusinessType.Partnership ⇒ partnership(request, url)
    }
  }

  private def removeScriptTags(html: String) = html.replaceAll("<script[\\s\\S]*?/script>", "")

  def partnership(implicit request: SummaryRequest[AnyContent], baseUrl: String) = {
    val application = Journeys partnershipApplication request

    partnership_summary(application, request.bpr, baseUrl)
  }


  def soleTrader(implicit request: SummaryRequest[AnyContent], baseUrl: String) = {
    val application = Journeys soleTraderApplication request

    sole_proprietor_summary(application, request.bpr, baseUrl)
  }

  private def ltdSummary(implicit request: SummaryRequest[AnyContent], baseUrl: String) = {
      val application = Journeys ltdApplication request

    ltd_summary(application, request.bpr, baseUrl)
  }
}
