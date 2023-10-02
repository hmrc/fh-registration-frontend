package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.{Address, AddressRecord, Country}


case class AddressLookupStub()
                            (implicit builder: PreconditionBuilder)
{

  private val address1 = AddressRecord(
    id = "test1",
    1233451L,
    Address(Seq("1 test drive", "testing"), Some("example"), "AA1 2BB", Country("GB", "United Kingdom")), "en")

  private val address2 = AddressRecord(
    id = "test2",
    1233452L,
    Address(Seq("2 test drive", "testing"), Some("example"), "AA1 2BB", Country("GB", "United Kingdom")), "en")

  private val address3 = AddressRecord(
    id = "test3",
    1233453L,
    Address(Seq("3 test drive", "testing"), Some("example"), "AA1 2BB", Country("GB", "United Kingdom")), "en")


  private val addressList = List(address1, address2, address3)
  private val singleAddressList = List(address1)

  private val addressListJson = Json.toJson(addressList)
  private val singleAddressListJson = Json.toJson(singleAddressList)

  def lookupAddress(isSuccess: Boolean = true) = {
    val response = if (isSuccess) {
      okJson(
        addressListJson.toString()
      )
    } else {
      serverError()
    }
    stubFor(
      post(urlPathEqualTo(
        s"/lookup")
      )
        .willReturn(response)
    )
    builder
  }

  def lookupSingleAddress(isSuccess: Boolean = true): PreconditionBuilder = {
    val response = if (isSuccess) {
      okJson(
        singleAddressListJson.toString
      )
    } else {
      serverError()
    }
    stubFor(
      post(urlPathEqualTo(
        s"/lookup")
      )
        .willReturn(response)
    )
    builder
  }

  def lookupNoAddress(isSuccess: Boolean = true): PreconditionBuilder = {
    val response = if (isSuccess) {
      okJson(
        JsArray().toString
      )
    } else {
      serverError()
    }
    stubFor(
      post(urlPathEqualTo(
        s"/lookup")
      )
        .willReturn(response)
    )
    builder
  }
}
