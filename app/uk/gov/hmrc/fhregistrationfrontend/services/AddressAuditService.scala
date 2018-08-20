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

package uk.gov.hmrc.fhregistrationfrontend.services

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import org.apache.commons.lang3.StringUtils
import play.api.Logger
import uk.gov.hmrc.fhregistrationfrontend.connectors.AddressLookupConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.AddressRecord
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent
import uk.gov.hmrc.play.audit.AuditExtensions.auditHeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@ImplementedBy(classOf[DefaultAddressAuditService])
trait AddressAuditService {
  def auditAddresses(page: String, addresses: List[Address])(implicit headerCarrier: HeaderCarrier): Future[Any]
}

object AddressAuditService {
  val AuditTypePostcodeAddressModifiedSubmitted = "postcodeAddressModifiedSubmitted"
  val AuditTypeManualAddressSubmitted = "manualAddressSubmitted"
  val AuditTypePostcodeAddressSubmitted = "postcodeAddressSubmitted"

  val AuditSource = "fh-registration-frontend"
}

private case class AddressAuditData(auditType: String, details: Seq[(String, String)])

@Singleton
class DefaultAddressAuditService  @Inject()(
  addressLookupConnector: AddressLookupConnector,
  auditConnector: AuditConnector
) extends AddressAuditService  {

  override def auditAddresses(page: String, addresses: List[Address])(implicit headerCarrier: HeaderCarrier): Future[Any] = {
    Logger.info(s"Auditing: $page, $addresses")
    val auditResults = addresses map { address ⇒
      addressAuditData(address)
        .flatMap(sendAuditEvent(page, _))
        .recover({ case t ⇒ AuditResult.Failure("failed to generate the event", Some(t))})
    }

    Future sequence auditResults
  }

  private def logAuditResult(auditResult: AuditResult) = auditResult match {
    case AuditResult.Success ⇒ true
    case AuditResult.Disabled ⇒ false
    case AuditResult.Failure(msg, None) ⇒
      Logger error s"Address audit failed: $msg"
      false
    case AuditResult.Failure(msg, Some(t)) ⇒
      Logger error (s"Address audit failed: $msg", t)
      false
  }

  private def sendAuditEvent(page: String, addressAuditData: AddressAuditData)(implicit headerCarrier: HeaderCarrier) = {
    val event = DataEvent(
      AddressAuditService.AuditSource,
      addressAuditData.auditType,
      tags = headerCarrier.toAuditTags("fh-registration", s"/fhdds/form/$page"),
      detail = headerCarrier.toAuditDetails(addressAuditData.details: _*)
    )
    Logger.info(s"Submitting event $event")
    auditConnector sendEvent event
  }

  private def addressAuditData(address: Address)(implicit headerCarrier: HeaderCarrier) = {
    address.lookupId match {
      case None ⇒ Future successful manualAddressSubmitted(address)
      case Some(id) ⇒ postcodeAddress(id, address)
    }
  }

  private def postcodeAddress(id: String, address: Address)(implicit headerCarrier: HeaderCarrier): Future[AddressAuditData] = {
    addressLookupConnector lookupById id map {
      case Some(originalAddressRecord)  ⇒
        val originalAddress = addressRecordToAddress(originalAddressRecord)
        if (sameUkAddresses(address, originalAddress))
          postcodeAddressSubmitted(address, originalAddressRecord.uprn.toString)
        else
          postcodeAddressModifiedSubmitted(address, originalAddress, originalAddressRecord.uprn.toString)
      case None                   ⇒
        Logger.error(s"Could not find address by id $id")
        manualAddressSubmitted(address)
    }
  }

  private def sameUkAddresses(address: Address, otherAddress: Address) = {
    address.copy(countryCode = None) == otherAddress.copy(countryCode = None)
  }

  private def manualAddressSubmitted(address: Address)(implicit headerCarrier: HeaderCarrier) =
    AddressAuditData(
      AddressAuditService.AuditTypeManualAddressSubmitted,
      addressDetails("submitted", address)
    )

  private def postcodeAddressSubmitted(address: Address, originalUprn: String)(implicit headerCarrier: HeaderCarrier) =
    AddressAuditData(
      AddressAuditService.AuditTypePostcodeAddressSubmitted,
      addressDetails("submitted", address) :+ ("submittedUPRN" → originalUprn)
    )

  private def postcodeAddressModifiedSubmitted(address: Address, originalAddress: Address, originalUprn: String)(implicit headerCarrier: HeaderCarrier): AddressAuditData = {

    AddressAuditData(
      AddressAuditService.AuditTypePostcodeAddressModifiedSubmitted,
      addressDetails("submitted", address) ++ addressDetails("original", originalAddress) :+ ("originalUPRN" → originalUprn))
  }

  private def addressDetails(prefix: String, address: Address) = {
    Seq(
      "Line1" → address.addressLine1,
      "Line2" → address.addressLine2.getOrElse(""),
      "Line3" → address.addressLine3.getOrElse(""),
      "Line4" → address.addressLine4.getOrElse(""),
      "Postcode" → address.postcode,
      "Country" → address.countryCode.getOrElse("")
    ).map { case (k, v) ⇒ s"$prefix$k" -> v}
  }


  private def addressRecordToAddress(addressRecord: AddressRecord) = {
    Address(
      addressRecord.address.line1,
      Some(addressRecord.address.line2) filterNot StringUtils.isBlank,
      Some(addressRecord.address.line3) filterNot StringUtils.isBlank,
      addressRecord.address.town,
      addressRecord.address.postcode,
      Some(addressRecord.address.country.code),
      Some(addressRecord.id)


    )
  }

}
