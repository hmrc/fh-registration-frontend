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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import java.io.IOException

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.fhregistrationfrontend.connectors.{AddressLookupConnector, AddressLookupErrorResponse, AddressLookupSuccessResponse}
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.{Address, AddressRecord, Country, RecordSet}
import uk.gov.hmrc.http.BadRequestException

class AddressLookupControllerSpec extends ControllerSpecWithGuiceApp {

  val mockAddressLookupConnector = mock[AddressLookupConnector]
  val controller =
    new AddressLookupController(mockAddressLookupConnector, mockMcc)(scala.concurrent.ExecutionContext.Implicits.global)

  "Address lookup controller" should {
    "Fail with wrong postcode" in {
      val action = controller.addressLookup("some postcode", None)

      val result = action.apply(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }

    "Fail when address lookup connector fails" in {
      val action = controller.addressLookup("AA1 1AA", None)

      when(mockAddressLookupConnector.lookup(any(), any())(any())) thenReturn AddressLookupErrorResponse(
        new BadRequestException("unkown"))

      val result = action.apply(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }

    "Fail when address lookup connector fails with unknown " in {
      val action = controller.addressLookup("AA1 1AA", None)

      when(mockAddressLookupConnector.lookup(any(), any())(any())) thenReturn AddressLookupErrorResponse(
        new IOException())

      val result = action.apply(FakeRequest())
      status(result) shouldBe BAD_GATEWAY
    }

    "Return the result received from the address lookup service" in {
      implicit val reads = Json.format[RecordSet]
      val response = RecordSet(
        Seq(
          AddressRecord(
            "id1",
            123342,
            Address(Seq("Line1", "Line2"), Some("town"), "AA1 1AA", Country("GB", "Great Britain")),
            "en")
        )
      )
      val action = controller.addressLookup("AA1 1AA", None)

      when(mockAddressLookupConnector.lookup(any(), any())(any())) thenReturn AddressLookupSuccessResponse(response)

      val result = action.apply(FakeRequest())

      status(result) shouldBe OK

      jsonBodyOf(result).as[RecordSet] shouldBe response
    }
  }
}
