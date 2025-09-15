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

import org.mockito.ArgumentCaptor
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.fhregistrationfrontend.connectors.AddressLookupConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.{Address => LookupAddress, AddressRecord, Country}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.Future

class AddressAuditServiceSpecs
    extends UnitSpec with ScalaFutures with MockitoSugar with BeforeAndAfterEach with Matchers {

  val auditConnector = mock[AuditConnector]
  val addressLookupConnector = mock[AddressLookupConnector]
  implicit val hc: HeaderCarrier = HeaderCarrier()

  val addressAuditService = new DefaultAddressAuditService(addressLookupConnector, auditConnector)(
    using scala.concurrent.ExecutionContext.Implicits.global
  )
  val ac: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(auditConnector)
    reset(addressLookupConnector)
  }

  "auditAddresses" should {

    "send a manualAddressSubmitted" when {
      "lookup id is not present" in {
        val address = Address("line1", Some("line2"), None, Some("Worthing"), "BN11 1XX", None, None)
        setupAuditConnector()

        auditEventFor(address).auditType shouldBe "manualAddressSubmitted"

      }

      "lookup id is not found in address lookup" in {
        val address = Address("line1", Some("line2"), None, Some("Worthing"), "BN11 1XX", None, Some("1234"))

        setupAddressLookupConnector(addressRecord = None)
        setupAuditConnector()

        auditEventFor(address).auditType shouldBe "manualAddressSubmitted"
      }
    }

    "send a postcodeAddressSubmitted message when the address matches" in {
      val address = Address("line1", Some("line2"), None, Some("Worthing"), "BN11 1XX", None, Some("1234"))

      val addressRecord = AddressRecord(
        "1234",
        12345,
        LookupAddress(
          Seq(address.addressLine1, address.addressLine2.get),
          town = address.addressLine4,
          postcode = address.postcode,
          country = Country("GB", "")
        ),
        "en"
      )

      setupAddressLookupConnector(addressRecord = Some(addressRecord))
      setupAuditConnector()

      auditEventFor(address).auditType shouldBe "postcodeAddressSubmitted"
    }

    "send a postcodeAddressModifiedSubmitted message when the address don't match" in {
      val address = Address("line1", Some("line2"), Some(""), Some("Worthing"), "BN11 1XX", None, Some("1234"))

      val addressRecord = AddressRecord(
        "1234",
        12345,
        LookupAddress(
          Seq(address.addressLine1 + " changed", address.addressLine2.get),
          town = address.addressLine4,
          postcode = address.postcode,
          country = Country("GB", "")
        ),
        "en"
      )

      setupAddressLookupConnector(addressRecord = Some(addressRecord))
      setupAuditConnector()
      auditEventFor(address).auditType shouldBe "postcodeAddressModifiedSubmitted"
    }
  }

  def auditEventFor(address: Address) = {
    await(addressAuditService `auditAddresses` ("contact", List(address)))
    verify(auditConnector).sendEvent(ac.capture())(using any(), any())
    ac.getValue
  }

  def setupAddressLookupConnector(addressRecord: Option[AddressRecord]) =
    when(addressLookupConnector.lookupById(any())(using any())).thenReturn(Future successful addressRecord)

  def setupAuditConnector() =
    when(auditConnector.sendEvent(any())(using any(), any())).thenReturn(Future successful AuditResult.Success)
}
