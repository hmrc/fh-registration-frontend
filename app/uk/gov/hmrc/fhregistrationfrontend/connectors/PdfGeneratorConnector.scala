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

package uk.gov.hmrc.fhregistrationfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.hmrc.fhregistrationfrontend.config.WSHttp
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.Future

@Singleton
class PdfGeneratorConnector @Inject() (ws: WSClient) extends PdfGeneratorConnect with ServicesConfig {
  val serviceURL: String = baseUrl("pdf-generator-service") + "/pdf-generator-service/generate"
  val http = WSHttp
  def getWsClient:WSClient = ws
}

trait PdfGeneratorConnect {

  val serviceURL: String
  val http: WSHttp
  def getWsClient: WSClient

  def generatePdf(html: String)(implicit hc: HeaderCarrier): Future[WSResponse] = {
    getWsClient.url(serviceURL).post(Map("html" -> Seq(html)))
  }

}