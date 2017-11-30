/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.fhddsfrontend.connectors

import play.api.http.Status
import play.api.mvc.Request
import uk.gov.hmrc.fhddsfrontend.config.WSHttp
import uk.gov.hmrc.fhddsfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhddsfrontend.models.businessregistration.BusinessRegistrationDetails.formats
import uk.gov.hmrc.http.HttpGet
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.frontend.filters.SessionCookieCryptoFilter
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.partials.HeaderCarrierForPartialsConverter

import scala.concurrent.Future

trait BusinessCustomerFrontendConnector extends ServicesConfig with HeaderCarrierForPartialsConverter {

  def serviceUrl = baseUrl("business-customer-frontend")
  val businessCustomerUri = "business-customer"
  val reviewDetailsUri = "fetch-review-details"
  val service = "FHDDS"//TODO replace with FHDDS
  val http: HttpGet

  def getReviewDetails(implicit request: Request[_]): Future[BusinessRegistrationDetails] = {
    val getUrl = s"$serviceUrl/$businessCustomerUri/$reviewDetailsUri/$service"
    http.GET(getUrl).map(respone =>
      respone.status match {
        case Status.OK =>
          val reviewDetails = respone.json.as[BusinessRegistrationDetails]
          reviewDetails
        case _ ⇒
          throw new IllegalArgumentException(respone.toString)
      })
//    http.withTracing[BusinessRegistrationDetails]("GET", getUrl) (http.GET[BusinessRegistrationDetails](getUrl))
  }
}

object BusinessCustomerFrontendConnector extends BusinessCustomerFrontendConnector {
  val http = WSHttp
  override def crypto: (String) => String = SessionCookieCryptoFilter.encrypt _
}
