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

import play.Logger
import play.api.{Configuration, Environment}
import play.api.libs.json.JsValue
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.{AddressRecord, RecordSet}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future

sealed trait AddressLookupResponse

case class AddressLookupSuccessResponse(addressList: RecordSet) extends AddressLookupResponse
case class AddressLookupErrorResponse(cause: Exception) extends AddressLookupResponse

@Singleton
class AddressLookupConnector @Inject() (
  val http: HttpClient,
  override val runModeConfiguration: Configuration,
  environment: Environment
) extends ServicesConfig with HttpErrorFunctions {


  override protected def mode = environment.mode
  val addressLookupUrl: String = baseUrl("address-lookup")

  def lookup(postcode: String, filter: Option[String])(implicit hc: HeaderCarrier): Future[AddressLookupResponse] = {
    val fhddsHc = hc.withExtraHeaders("X-Hmrc-Origin" -> "FHDDS")
    http.GET[JsValue](s"$addressLookupUrl/uk/addresses?postcode=$postcode&filter=${filter.getOrElse("")}")(implicitly[HttpReads[JsValue]], fhddsHc, MdcLoggingExecutionContext.fromLoggingDetails(hc)
    ) map {
      addressListJson =>
        AddressLookupSuccessResponse(RecordSet.fromJsonAddressLookupService(addressListJson))
    } recover {
      case e: Exception =>
        Logger.warn(s"Error received from address lookup service: $e")
        AddressLookupErrorResponse(e)
    }
  }

  def lookupById(id: String)(implicit hc: HeaderCarrier): Future[Option[AddressRecord]] = {
    val fhddsHc = hc.withExtraHeaders("X-Hmrc-Origin" -> "FHDDS")
    http.GET[Option[AddressRecord]](s"$addressLookupUrl/uk/addresses/$id")(addressRecordReads, fhddsHc, MdcLoggingExecutionContext.fromLoggingDetails(hc))
  }


  private val addressRecordReads = new HttpReads[Option[AddressRecord]] {
    override def read(method: String, url: String, response: HttpResponse): Option[AddressRecord] = {
      response.status match {
        case status if status == 200 ⇒ Some(response.json.as[AddressRecord])
        case status if status == 404 ⇒ None
        case status if is4xx(status) ⇒ throw Upstream4xxResponse("address-lookup/uk/address error", response.status, 500)
        case status if is5xx(status) ⇒ throw Upstream5xxResponse("address-lookup/uk/address error", response.status, 502)
      }
    }
  }

}


