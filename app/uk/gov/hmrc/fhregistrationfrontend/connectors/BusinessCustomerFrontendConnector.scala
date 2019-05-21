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

package uk.gov.hmrc.fhregistrationfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment}
import play.api.mvc.Request
import uk.gov.hmrc.crypto.PlainText
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails.formats
import uk.gov.hmrc.play.bootstrap.config.RunMode
import uk.gov.hmrc.play.bootstrap.filters.frontend.crypto.SessionCookieCrypto
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.play.partials.HeaderCarrierForPartialsConverter
import scala.concurrent.Future

@Singleton
class BusinessCustomerFrontendConnector @Inject() (
  val http: HttpClient,
  val runModeConfiguration: Configuration,
  val runMode: RunMode,
  environment: Environment,
  sessionCookieCrypto: SessionCookieCrypto
) extends ServicesConfig(runModeConfiguration, runMode) with HeaderCarrierForPartialsConverter {

  def serviceUrl = baseUrl("business-customer-frontend")
  val businessCustomerUri = "business-customer"
  val reviewDetailsUri = "fetch-review-details"
  val service = "FHDDS"

  override def crypto: (String) => String = { v ⇒
    sessionCookieCrypto.crypto.encrypt(PlainText(v)).value
  }

  def getReviewDetails(implicit request: Request[_]): Future[BusinessRegistrationDetails] = {
    val getUrl = s"$serviceUrl/$businessCustomerUri/$reviewDetailsUri/$service"
    http.GET[BusinessRegistrationDetails](getUrl)
  }
}

