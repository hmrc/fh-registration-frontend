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

import javax.inject.Singleton

import play.Logger
import play.api.libs.json.JsValue
import uk.gov.hmrc.fhregistrationfrontend.config.WSHttp
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.RecordSet
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

sealed trait AddressLookupResponse

case class AddressLookupSuccessResponse(addressList: RecordSet) extends AddressLookupResponse
case class AddressLookupErrorResponse(cause: Exception) extends AddressLookupResponse

@Singleton
class AddressLookupConnector extends AddressLookupConnect with ServicesConfig {
  val addressLookupUrl: String = baseUrl("address-lookup")
  val http = WSHttp
}

trait AddressLookupConnect {

  val addressLookupUrl: String
  val http: WSHttp

  def lookup(postcode: String)(implicit hc: HeaderCarrier): Future[AddressLookupResponse] = {
    val fhddsHc = hc.withExtraHeaders("X-Hmrc-Origin" -> "FHDDS")
    http.GET[JsValue](s"$addressLookupUrl/uk/addresses?postcode=$postcode")(implicitly[HttpReads[JsValue]], fhddsHc, MdcLoggingExecutionContext.fromLoggingDetails(hc)
    ) map {
      addressListJson =>
        AddressLookupSuccessResponse(RecordSet.fromJsonAddressLookupService(addressListJson))
    } recover {
      case e: Exception =>
        Logger.warn(s"Error received from address lookup service: $e")
        AddressLookupErrorResponse(e)
    }
  }
}