package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.CompositeSymmetricCrypto.aes
import uk.gov.hmrc.crypto.{CompositeSymmetricCrypto, PlainText}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{CompanyOfficer, CompanyOfficerIndividual, CompanyOfficerType, ListWithTrackedChanges}

case class Save4LaterStub
()
  (implicit builder: PreconditionBuilder) {

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

  val contactPerson =
    """
      |{
      |  "firstName" : "firstName",
      |  "lastName" : "lastName",
      |  "jobTitle" : "jobTitle",
      |  "telephone" : "07012311234",
      |  "emailAddress" : "er@hotm.com",
      |  "usingSameContactAddress" : true
      |}
    """.stripMargin

  val companyOfficials = ListWithTrackedChanges(
    valuesWithStatus = List(
      (CompanyOfficer(
        officialType = CompanyOfficerType.Individual,
        identification = CompanyOfficerIndividual(
          firstName = "firstName",
          lastName = "lastName",
          hasNino = true,
          nino = Some("AA123123A"),
          hasPassportNumber = None,
          passport = None,
          nationalId = None,
          role = "Director"
        )
      ),
        ListWithTrackedChanges.NoChange)
    ),
    deleted = List.empty
  )

  def businessRecordHasSaved() = {
    stubFor(
      stubS4LPut("userLastTimeSaved")
    )
    stubFor(
      stubS4LPut("businessRegistrationDetails", brd)
    )
    builder
  }

  def savePageData(key: String, data: String = "") = {
    stubFor(
      stubS4LGetSetJs(
        s"""{
           "businessRegistrationDetails": "${encrypt(brd)}"
           ,
           "businessType": "${encrypt("\"CorporateBody\"")}",
           "$key": "${encrypt(data)}"
           }""")
    )
    builder
  }

  def getNoneData() = {
    stubFor(
      stubS4LGet()
    )
    builder
  }

  def getFullData() = {
    stubFor(
      stubS4LGetSetJs(
        s"""{
           "userLastTimeSaved": "${encrypt("\"data\"")}",
           "businessRegistrationDetails": "${encrypt(brd)}",
           "businessType": "${encrypt("\"CorporateBody\"")}",
           "mainBusinessAddress": "${encrypt("""{"timeAtCurrentAddress": "3-5 years"}""")}",
           "contactPerson": "${encrypt(contactPerson)}",
           "companyRegistrationNumber": "${encrypt("""{"crn": "12345678"}""")}",
           "dateOfIncorporation": "${encrypt("""{"dateOfIncorporation": "2222-02-22"}""")}",
           "tradingName": "${encrypt("""{"hasValue": false}""")}",
           "vatNumber": "${encrypt("""{"hasValue": false}""")}",
           "companyOfficers": "${encrypt(Json.toJson(companyOfficials).toString())}",
           "businessStatus": "${encrypt("""{"isNewFulfilmentBusiness": false}""")}",
           "importingActivities": "${encrypt("""{"hasEori": false}""")}",
           "businessCustomers": "${encrypt("""{"numberOfCustomers": "1-10"}""")}",
           "otherStoragePremises": "${encrypt("""{"hasValue": false, "value":{"valuesWithStatus":[], "deleted":[]}}""")}"
           }""")
    )
    builder
  }

  def businessTypeHasSaved() = {
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