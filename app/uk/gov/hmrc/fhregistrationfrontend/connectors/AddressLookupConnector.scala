/*
 * Copyright 2021 HM Revenue & Customs
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

import com.google.inject.ImplementedBy

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Environment, Logging}
import play.api.libs.json.JsValue
import play.api.libs.json.{Json, OFormat, Writes}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.{AddressRecord, Country, RecordSet}
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpReads.Implicits._

import uk.gov.hmrc.play.validators.AddressFields.{addressLine4, postcode}

import scala.concurrent.{ExecutionContext, Future}

sealed trait AddressLookupResponse

case class AddressLookupSuccessResponse(addressList: RecordSet) extends AddressLookupResponse
case class AddressLookupErrorResponse(cause: Exception) extends AddressLookupResponse
case class LookupAddressByPostcode(postcode: String, filter: Option[String])

@Singleton
class AddressLookupConnector @Inject()(
  val http: HttpClient,
  val runModeConfiguration: Configuration,
  environment: Environment,
  frontendAppConfig: FrontendAppConfig
)(implicit val ec: ExecutionContext)
    extends ServicesConfig(runModeConfiguration) with HttpErrorFunctions with Logging {

  val addressLookupUrl: String = baseUrl("address-lookup")

  val endpoint = frontendAppConfig.addressReputationEndpoint

  def lookup(postcode: String, filter: Option[String])(implicit hc: HeaderCarrier): Future[AddressLookupResponse] = {
    logger.info("Lookup function is being called")
    val lookupAddressByPostcode = LookupAddressByPostcode(postcode, filter)
    http
      .POST[LookupAddressByPostcode, List[AddressRecord]](s"$endpoint/lookup", lookupAddressByPostcode)
      .map { found =>
        val results = found.map { address =>
          AddressRecord(
            address.id,
            address.uprn,
            address.address,
            address.language
          )
        }
        val addressRec = RecordSet(results)
        AddressLookupSuccessResponse(addressRec)
      } recover {
      case e: Exception =>
        logger.warn(s"Error received from address lookup service: $e")
        AddressLookupErrorResponse(e)
    }
  }

//  def lookup(postcode: String, filter: Option[String])(implicit hc: HeaderCarrier): Future[AddressLookupResponse] = {
//    logger.info("Lookup function is being called")
//    val lookupAddressByPostcode = LookupAddressByPostcode(postcode, filter)
//    http
//      .POST[LookupAddressByPostcode, AddressRecord](s"$endpoint/lookup", lookupAddressByPostcode)
//      .map { addressListJson =>
//        AddressLookupSuccessResponse(RecordSet.fromJsonAddressLookupService(Json.toJson(addressListJson)))
//      } recover {
//      case e: Exception =>
//        logger.warn(s"Error received from address lookup service: $e")
//        AddressLookupErrorResponse(e)
//    }
//  }

  def lookupById(id: String)(implicit hc: HeaderCarrier): Future[Option[AddressRecord]] = {
    val fhddsHc = hc.withExtraHeaders("X-Hmrc-Origin" -> "FHDDS")
    http.GET[Option[AddressRecord]](s"$addressLookupUrl/v2/uk/addresses/$id")(addressRecordReads, fhddsHc, ec)
  }

  private val addressRecordReads = new HttpReads[Option[AddressRecord]] {
    override def read(method: String, url: String, response: HttpResponse): Option[AddressRecord] =
      response.status match {
        case status if status == 200 ⇒ Some(response.json.as[AddressRecord])
        case status if status == 404 ⇒ None
        case status if is4xx(status) ⇒
          throw UpstreamErrorResponse("address-lookup/v2/uk/address error", response.status, 500)
        case status if is5xx(status) ⇒
          throw UpstreamErrorResponse("address-lookup/v2/uk/address error", response.status, 502)
      }
  }
}

object LookupAddressByPostcode {
  implicit val writes: Writes[LookupAddressByPostcode] = Json.writes[LookupAddressByPostcode]
}
