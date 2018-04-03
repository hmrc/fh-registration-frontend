package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import uk.gov.hmrc.crypto.CompositeSymmetricCrypto.aes
import uk.gov.hmrc.crypto.{CompositeSymmetricCrypto, PlainText}

case class KeyStoreStub
()
  (implicit builder: PreconditionBuilder) {


  def businessRecordHasSaved() = {
    stubFor(
      stubS4LPut("userLastTimeSaved")
    )
    stubFor(
      stubS4LPut("businessRegistrationDetails")
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
    stubFor(
      stubS4LGet("businessType", "CorporateBody")
    )
    builder
  }

  val crypto: CompositeSymmetricCrypto = aes(s"fqpLDZ4sumDsekHkeEBlCA==", Seq.empty)

  def encrypt(str: String): String = crypto.encrypt(PlainText(str)).value

  def stubS4LPut(key: String, data: String = "data"): MappingBuilder =
    put(urlPathMatching(s"/keystore/fh-registration-frontend/some-id/data/$key"))
      .willReturn(ok(s"""
                        |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
                        |    "data": { "${encrypt(key)}": "${encrypt(data)}" },
                        |    "id": "some-id",
                        |    "modifiedDetails": {
                        |      "createdAt": { "$$date": 1502265526026 },
                        |      "lastUpdated": { "$$date": 1502265526026 }}}
          """.stripMargin
      ))

  def stubS4LGet(key: String = "", data: String = ""): MappingBuilder =
    get(urlPathMatching("/keystore/fh-registration-frontend/some-id"))
      .willReturn(ok(
        s"""
           |{
           |  "atomicId": { "$$oid": "598830cf5e00005e00b3401e" },
           |  "data": {
           |    "$key": "$data"
           |  },
           |  "id": "some-id",
           |  "modifiedDetails": {
           |    "createdAt": { "$$date": 1502097615710 },
           |    "lastUpdated": { "$$date": 1502189409725 }
           |  }
           |}
          """.stripMargin
      ))

}