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

package uk.gov.hmrc.fhregistrationfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.mvc.Request
import uk.gov.hmrc.play.partials.HeaderCarrierForPartialsConverter

import scala.concurrent.ExecutionContext
import play.api.{Configuration, Environment}
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.Future

@Singleton
class BusinessCustomerFrontendConnector @Inject()(
  val http: HttpClient,
  val configuration: Configuration,
  environment: Environment
) extends ServicesConfig(configuration) with HeaderCarrierForPartialsConverter {

  def serviceUrl = baseUrl("business-customer-frontend")
  val businessCustomerUri = "business-customer"
  val reviewDetailsUri = "fetch-review-details"
  val service = "FHDDS"

  //upgrading to play 2.6.21 has caused for the encryption to be executed twice,
  //however we still need the functionality of HeaderCarrierForPartials so we the cookie can be found.
  override def crypto: (String) => String = { v ⇒
    v
  }

  def getReviewDetails(implicit request: Request[_], ec: ExecutionContext): Future[BusinessRegistrationDetails] = {
    val getUrl = s"$serviceUrl/$businessCustomerUri/$reviewDetailsUri/$service"
    http.GET[BusinessRegistrationDetails](getUrl)
  }
}
