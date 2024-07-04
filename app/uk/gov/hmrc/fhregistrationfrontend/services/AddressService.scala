/*
 * Copyright 2023 HM Revenue & Customs
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

import org.apache.commons.lang3.StringUtils
import uk.gov.hmrc.fhregistrationfrontend.connectors.{AddressLookupConnector, AddressLookupErrorResponse, AddressLookupSuccessResponse}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.AddressRecord
import uk.gov.hmrc.http.HeaderCarrier

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressService @Inject() (
  addressLookupConnector: AddressLookupConnector,
  addressAuditService: AddressAuditService
)(implicit ec: ExecutionContext) {

  def addressLookup(path: String, postcode: String, filter: Option[String])(implicit
    hc: HeaderCarrier
  ): Future[Either[AddressLookupErrorResponse, Map[String, Address]]] =
    addressLookupConnector.lookup(postcode, filter).map {
      case AddressLookupSuccessResponse(addressList) =>
        addressAuditService.auditAddressesFromRecordSet(path, addressList)
        Right(
          addressList.addresses
            .map(addressRecord => UUID.randomUUID().toString -> addressRecordToAddress(addressRecord))
            .toMap
        )
      case e: AddressLookupErrorResponse => Left(e)
    }

  private def addressRecordToAddress(addressRecord: AddressRecord) =
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
