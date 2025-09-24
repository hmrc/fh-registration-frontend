/*
 * Copyright 2025 HM Revenue & Customs
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

import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.fhregistrationfrontend.connectors.*
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.{Address => ModelAddress, AddressRecord, Country, RecordSet}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AddressServiceSpec extends AsyncWordSpec with Matchers with MockitoSugar with ScalaFutures {

  given HeaderCarrier = HeaderCarrier()

  "AddressService.addressLookup" should {

    "return Right with mapped addresses when connector succeeds" in {
      val mockConnector = mock[AddressLookupConnector]
      val mockAuditService = mock[AddressAuditService]
      val service = new AddressService(mockConnector, mockAuditService)

      val modelAddress = ModelAddress(
        lines = Seq("10 Downing Street", "Westminster"),
        town = Some("London"),
        postcode = "SW1A 2AA",
        country = Country("GB", "United Kingdom")
      )

      val addressRecord = AddressRecord(
        id = "id-123",
        uprn = 11111111L,
        address = modelAddress,
        language = "en"
      )

      val recordSet = RecordSet(Seq(addressRecord))
      val response = AddressLookupSuccessResponse(recordSet)

      when(mockConnector.lookup(eqTo("SW1A 2AA"), any())(using any[HeaderCarrier]))
        .thenReturn(Future.successful(response))
      when(mockAuditService.auditAddressesFromRecordSet(any(), any())(using any[HeaderCarrier]))
        .thenReturn(Future.successful(()))

      val resultF = service.addressLookup("/test-path", "SW1A 2AA", None)

      resultF.map { result =>
        result.isRight shouldBe true
        val addresses: Map[String, Address] = result.toOption.get

        val actual: Address = addresses.values.head
        val expected: Address = Address(
          addressLine1 = "10 Downing Street",
          addressLine2 = Some("Westminster"),
          addressLine3 = None,
          addressLine4 = Some("London"),
          postcode = "SW1A 2AA",
          countryCode = Some("GB"),
          lookupId = Some("id-123")
        )

        actual shouldBe expected
        verify(mockAuditService).auditAddressesFromRecordSet(eqTo("/test-path"), eqTo(recordSet))(using any())

        succeed
      }
    }

    "return Left when connector returns error" in {
      val mockConnector = mock[AddressLookupConnector]
      val mockAuditService = mock[AddressAuditService]
      val service = new AddressService(mockConnector, mockAuditService)

      val exception = new RuntimeException("lookup failure")
      val errorResponse = AddressLookupErrorResponse(exception)

      when(mockConnector.lookup(eqTo("INVALID"), any())(using any[HeaderCarrier]))
        .thenReturn(Future.successful(errorResponse))

      val resultF = service.addressLookup("/bad-path", "INVALID", None)

      resultF.map { result =>
        result shouldBe Left(errorResponse)
        verifyNoInteractions(mockAuditService)
        succeed
      }
    }
  }
}
