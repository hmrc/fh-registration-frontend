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

import javax.inject.Singleton

import play.api.libs.json.JsValue
import uk.gov.hmrc.fhddsfrontend.config.WSHttp
import uk.gov.hmrc.fhddsfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

@Singleton
class FhddsConnector extends FhddsConnect with ServicesConfig {
  val FHDSSServiceUrl: String = baseUrl("fhdds")
  val http = WSHttp
}

trait FhddsConnect {

  val FHDSSServiceUrl: String
  val http: WSHttp

  def saveBusinessRegistrationDetails(userId: String, formTypeRef: String, businessRegistrationDetails: BusinessRegistrationDetails)(implicit hc: HeaderCarrier): Future[_] = {
    http.PUT[BusinessRegistrationDetails,JsValue](saveBusinessRegistrationDetailsURI(userId, formTypeRef), businessRegistrationDetails)
  }

  private def saveBusinessRegistrationDetailsURI(userId: String, formTypeRef: String) = {
    s"$FHDSSServiceUrl/fhdds/submission-extra-data/$userId/$formTypeRef/businessRegistrationDetails"
  }


}