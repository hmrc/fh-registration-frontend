package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._

case class KeyStoreStub
()
  (implicit builder: PreconditionBuilder) {

  def fetchWithdrawalReason() = {
    stubFor(
      keyStoreGet("withdrawalReason", """{"withdrawalReason": "Applied in Error"}""")
    )
    builder
  }

  def saveWithdrawalReason() = {
    stubFor(
      keyStorePut("withdrawalReason", """{"withdrawalReason": "Applied in Error"}""")
    )
    builder
  }

  def acceptsDelete() = {
    stubFor(
      delete(urlPathMatching(s"/keystore/fh-registration-frontend/some-id/")).willReturn(ok())
    )
    builder
  }

  def keyStorePut(key: String, data: String = "data"): MappingBuilder =
    put(urlPathMatching(s"/keystore/fh-registration-frontend/some-id/data/$key"))
      .willReturn(ok(s"""
                        |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
                        |    "data": { "$key": $data },
                        |    "id": "some-id",
                        |    "modifiedDetails": {
                        |      "createdAt": { "$$date": 1502265526026 },
                        |      "lastUpdated": { "$$date": 1502265526026 }}}
          """.stripMargin
      ))

  def keyStoreGet(key: String = "", data: String = ""): MappingBuilder =
    get(urlPathMatching("/keystore/fh-registration-frontend/some-id"))
      .willReturn(ok(
        s"""
           |{
           |  "atomicId": { "$$oid": "598830cf5e00005e00b3401e" },
           |  "data": {
           |    "$key": $data
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