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

package uk.gov.hmrc.fhregistrationfrontend.connectors

import org.mockito.Mockito.*
import org.scalatest.wordspec.AsyncWordSpec
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.{Address, AddressRecord, Country, RecordSet}
import uk.gov.hmrc.http.*

import scala.concurrent.Future

class AddressLookupConnectorSpec extends HttpClientV2Helper {

  val mockConfig: Configuration = mock[Configuration]
  val mockFrontendAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  when(mockFrontendAppConfig.addressReputationEndpoint).thenReturn("http://test.address.lookup")

  val connector = new AddressLookupConnector(mockHttp, mockConfig, mockFrontendAppConfig)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockCountry = Country("GB", "Great Britain")
  val mockAddress = Address(Seq("Line1", "Line2"), Some("town"), "AA1 1AA", mockCountry)
  val mockAddressRecord1 = AddressRecord("id1", 123342, mockAddress, "en")
  val mockAddressRecord2 = AddressRecord("id2", 123343, mockAddress, "en")

  "AddressLookupConnector" when {

    "lookup is called" should {

      "return AddressLookupSuccessResponse with address list" in {
        val postcode = "AA1 1AA"
        val filter = Some("testFilter")
        val addressRecords = List(
          mockAddressRecord1,
          mockAddressRecord2
        )
        val expectedResponse = AddressLookupSuccessResponse(RecordSet(addressRecords))

        requestBuilderExecute(Future.successful(addressRecords))

        connector.lookup(postcode, filter).map { response =>
          response shouldBe expectedResponse
          val capturedJson = jsonCaptor.getValue.toString
          Json.parse(capturedJson) shouldBe Json.parse(
            s"""
               |{
               |  "postcode": "$postcode",
               |  "filter": "testFilter"
               |}
               |""".stripMargin
          )
        }
      }

      "return AddressLookupErrorResponse on exception" in {
        val postcode = "AA1 1AA"
        val filter = Some("testFilter")
        val exception = new RuntimeException("Test exception")

        requestBuilderExecute(Future.failed(exception))

        connector.lookup(postcode, filter).map { response =>
          response shouldBe AddressLookupErrorResponse(exception)
        }
      }
    }

    "lookupById is called" should {

      "return Some(AddressRecord) when found" in {
        val id = "id1"
        val addressRecord = mockAddressRecord1

        requestBuilderExecute(Future.successful(Array(addressRecord)))

        connector.lookupById(id).map { response =>
          response shouldBe Some(addressRecord)
        }
      }

      "return None when not found" in {
        val id = "id1"

        requestBuilderExecute(Future.successful(Array.empty[AddressRecord]))

        connector.lookupById(id).map { response =>
          response shouldBe None
        }
      }
    }
  }
}
