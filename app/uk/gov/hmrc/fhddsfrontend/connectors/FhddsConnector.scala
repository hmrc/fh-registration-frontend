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

import uk.gov.hmrc.fhddsfrontend.config.WSHttp
import uk.gov.hmrc.fhddsfrontend.models.FhddsModels._
import uk.gov.hmrc.fhddsfrontend.models._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.Future

@Singleton
class FhddsConnector extends FhddsConnect {
  val FHDSSServiceUrl: String = baseUrl("fhdds")
  val orgLookupURI: String = "fhdds/companyDetails"
  val http = WSHttp
}

trait FhddsConnect extends ServicesConfig  {

  val FHDSSServiceUrl: String
  val orgLookupURI: String
  val http: WSHttp

  def lookupCompanyDetails()(implicit hc: HeaderCarrier): Future[CompanyDetails] = {
    http.GET[CompanyDetails](s"$FHDSSServiceUrl/$orgLookupURI")
  }

}