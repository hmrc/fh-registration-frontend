package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import uk.gov.hmrc.crypto.CompositeSymmetricCrypto.aes
import uk.gov.hmrc.crypto.{CompositeSymmetricCrypto, PlainText}

case class Save4LaterStub
()
  (implicit builder: PreconditionBuilder) {


  def businessRecordHasSaved() = {
    stubFor(
      stubS4LPut("userLastTimeSaved")
    )
    stubFor(
      stubS4LPut("businessRegistrationDetails",
        """{
          |  "business_name" : "Real Business Inc",
          |  "business_type" : "corporate body",
          |  "business_address" : {
          |    "line1" : "23 High Street",
          |    "line2" : "Park View",
          |    "line3" : "Gloucester",
          |    "line4" : "Gloucestershire",
          |    "postcode" : "NE98 1ZZ",
          |    "country" : "GB"
          |  },
          |  "safe_id" : "XE0001234567890",
          |  "utr" : "1111111111"
          |}""")
    )
    builder
  }

  def getNoneData() = {
    stubFor(
      stubS4LGet()
    )
    builder
  }

  def businessTypeHasSaved() = {
    val brd =
      """
        |{
        |  "business_name" : "Real Business Inc",
        |  "business_type" : "corporate body",
        |  "business_address" : {
        |    "line1" : "23 High Street",
        |    "line2" : "Park View",
        |    "line3" : "Gloucester",
        |    "line4" : "Gloucestershire",
        |    "postcode" : "NE98 1ZZ",
        |    "country" : "GB"
        |    },
        |  "safe_id" : "XE0001234567890",
        |  "utr" : "1111111111"
        |}
      """.stripMargin

    stubFor(
      stubS4LGetSetJs(
        s"""{
           "businessRegistrationDetails": "${encrypt(brd)}"
           ,
           "businessType": "${encrypt("\"CorporateBody\"")}"
           }""")
    )
    builder
  }

  val crypto: CompositeSymmetricCrypto = aes(s"fqpLDZ4sumDsekHkeEBlCA==", Seq.empty)

  def encrypt(str: String): String = crypto.encrypt(PlainText(str)).value

  def stubS4LPut(key: String, data: String = "data"): MappingBuilder =
    put(urlPathMatching(s"/save4later/fh-registration-frontend/some-id/data/$key"))
      .willReturn(ok(
        s"""
           |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
           |    "data": { "${encrypt(key)}": "${encrypt(data)}" },
           |    "id": "some-id",
           |    "modifiedDetails": {
           |      "createdAt": { "$$date": 1502265526026 },
           |      "lastUpdated": { "$$date": 1502265526026 }}}
          """.stripMargin
      ))

  def stubS4LGet(key: String = "", data: String = ""): MappingBuilder =
    get(urlPathMatching("/save4later/fh-registration-frontend/some-id"))
      .willReturn(ok(
        s"""
           {
             "atomicId": { "$$oid": "598830cf5e00005e00b3401e" },
             "data": {
               "$key": "${encrypt(data)}"
             },
             "id": "some-id",
             "modifiedDetails": {
               "createdAt": { "$$date": 1502097615710 },
               "lastUpdated": { "$$date": 1502189409725 }
             }
           }
          """.stripMargin
      ))

  def stubS4LGetSetJs(data: String = s"""{"":""}"""): MappingBuilder = {
    println(s""""$data"""")
    get(urlPathMatching("/save4later/fh-registration-frontend/some-id"))
      .willReturn(ok(
        s"""
           |{
           |  "atomicId": { "$$oid": "598830cf5e00005e00b3401e" },
           |  "data": $data,
           |  "id": "some-id",
           |  "modifiedDetails": {
           |    "createdAt": { "$$date": 1502097615710 },
           |    "lastUpdated": { "$$date": 1502189409725 }
           |  }
           |}
          """.stripMargin
      ))
  }
}