package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._

case class Save4LaterStub
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
      stubS4LGetNone
    )
    builder
  }


  def stubS4LPut(key: String): MappingBuilder =
    put(urlPathMatching(s"/save4later/fh-registration-frontend/some-id/data/$key"))
      .willReturn(ok(s"""
                        |{ "atomicId": { "$$oid": "598ac0b64e0000d800170620" },
                        |    "data": { "key": "data}" },
                        |    "id": "some-id",
                        |    "modifiedDetails": {
                        |      "createdAt": { "$$date": 1502265526026 },
                        |      "lastUpdated": { "$$date": 1502265526026 }}}
          """.stripMargin
      ))

  def stubS4LGetNone: MappingBuilder =
    get(urlPathMatching("/save4later/fh-registration-frontend/some-id"))
      .willReturn(ok(
        s"""
           |{
           |  "atomicId": { "$$oid": "598830cf5e00005e00b3401e" },
           |  "data": {},
           |  "id": "1",
           |  "modifiedDetails": {
           |    "createdAt": { "$$date": 1502097615710 },
           |    "lastUpdated": { "$$date": 1502189409725 }
           |  }
           |}
          """.stripMargin
      ))

}