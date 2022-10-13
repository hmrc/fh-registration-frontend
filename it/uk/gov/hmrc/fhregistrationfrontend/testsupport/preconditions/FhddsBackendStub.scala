package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import java.util.Date
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json
import uk.gov.hmrc.fhregistration.models.fhdds.{SubmissionRequest, SubmissionResponse}
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.{Address, BusinessRegistrationDetails}
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress
import uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions.Save4LaterStub


case class FhddsBackendStub()
  (implicit builder: PreconditionBuilder)
{
  import BusinessRegistrationDetails.formats


  private val aFakeAddress = Address(
    line1 = "line1",
    line2 = "line2",
    line3 = None,
    line4 = None,
    postcode = Some("NE98 1ZZ"),
    country = "GB")

  private def mkBusinessPartnerRecord(businessType: String) = {
    BusinessRegistrationDetails(
      businessName = Some("Real Business Inc"),
      None,
      businessAddress = aFakeAddress,
      safeId = Some("XE0001234567890"),
      utr = Some("1111111111")
     )
  }

//  val contactPerson =
//    """
//      |{
//      |  "firstName" : "firstName",
//      |  "lastName" : "lastName",
//      |  "jobTitle" : "jobTitle",
//      |  "telephone" : "07012311234",
//      |  "emailAddress" : "er@hotm.com",
//      |  "usingSameContactAddress" : true
//      |}
//    """.stripMargin
//
//  private val formData = Map(
//    "mainBusinessAddress" -> """{"timeAtCurrentAddress": "3-5 years"}""",
//    "contactPerson" -> contactPerson,
//    "companyRegistrationNumber" -> """{"crn": "12345678"}""",
//    "dateOfIncorporation" -> """{"dateOfIncorporation": "2222-02-22"}""",
//    "tradingName" -> """{"hasValue": false}""",
//    "vatNumber" -> """{"hasValue": false}""",
//    "businessStatus" -> """{"isNewFulfilmentBusiness": false}""",
//    "importingActivities" -> """{"hasEori": false}""",
//    "businessCustomers" -> """{"numberOfCustomers": "1-10"}""",
//    "otherStoragePremises" -> """{"hasValue": false, "value": {"valuesWithStatus": [], "deleted": []}}"""
//  )
//
//
//  private val businessInformationData = Map(
//    "userLastTimeSaved" -> System.currentTimeMillis().toString,
//    "verifiedEmail" -> "\"user@test.com\"",
//    "businessType" -> "\"CorporateBody\""
//  )
//
//  private val displayDesDeclaration = Map(
//    "display_des_declaration" →
//      """
//        |{
//        |"personName": "John",
//        |"personStatus": "Director",
//        |"email": "user@test.com",
//        |"isInformationAccurate": true
//        |}
//    """.stripMargin
//  )
//
//  private val displayVerifiedEmail = Map(
//    "display_verifiedEmail" → "\"user@test.com\""
//  )
//  private val amendmentData =
//    businessInformationData ++ displayVerifiedEmail ++ displayDesDeclaration ++ formData ++ formData.map { case (k, v) ⇒ s"display_$k" → v } + ("isAmendment" → "true")
//
//  def stubS4LGet(data: Map[String, String]) = {
//    stubFor(get(urlPathMatching("/save4later/fh-registration-frontend/some-id"))
//      .willReturn(ok(
//        s"""
//           |{
//           |  "atomicId": { "$$oid": "598830cf5e00005e00b3401e" },
//           |  "data": ${asS4LData(data)},
//           |  "id": "some-id",
//           |  "modifiedDetails": {
//           |    "createdAt": { "$$date": 1502097615710 },
//           |    "lastUpdated": { "$$date": 1502189409725 }
//           |  }
//           |}
//          """.stripMargin
//      )))
//  }
//
//  private def asS4LData(data: Map[String, String]) = {
//    Json.toJson(
//      data.map {
//        case (k, v) ⇒ k → Json.toJson(encrypt(v))
//      }
//    ).toString()
//
//  }
//  val crypto: CompositeSymmetricCrypto = aes(s"fqpLDZ4sumDsekHkeEBlCA==", Seq.empty)
//
//  def encrypt(str: String): String = crypto.encrypt(PlainText(str)).value
//  def hasAmendmentData() = {
//    stubS4LGet(amendmentData)
//    builder
//  }
//  private def subscriptionInfo = {
//    SubscriptionDisplay(
//      organizationType: String,
//      FHbusinessDetail: IsNewFulfilmentBusiness,
//      additionalBusinessInformation: AdditionalBusinessInformationwithType,
//      businessDetail: BusinessDetail,
//      businessAddressForFHDDS: BusinessAddressForFHDDS,
//      contactDetail: ContactDetail,
//      declaration: Declaration)
//  }

  def hasBusinessDetails(formType: String, businessType: String) = {
    stubFor(
      put(urlPathEqualTo(
        s"/fhdds/submission-extra-data/some-id/$formType/businessRegistrationDetails")
      )
      .willReturn(ok(
        Json.toJson(mkBusinessPartnerRecord(businessType)).toString()
      ))
    )
    builder
  }

  def hasNoEnrolmentProgress() = {
    stubFor(
      get(urlEqualTo("/fhdds/subscription/enrolmentProgress"))
        .willReturn(ok(Json.toJson(EnrolmentProgress.Unknown).toString()))
    )
    builder
  }

  def acceptsAmendments() = {
    stubFor(
      post(
        urlPathMatching("/fhdds/subscription/amend/..FH[0-9A-Z]+"))
        .willReturn(
          ok(Json.toJson(SubmissionResponse("", new Date)).toString())))

    builder
  }

  def createSubscription() = {
    stubFor(
      post(
        urlPathMatching("/fhdds/subscription/subscribe/XE0001234567890"))
        .willReturn(
          ok(Json.toJson(SubmissionResponse("XEFH01234567890.", new Date)).toString())))

    builder
  }

  def getProcessingSubscription() = {
  stubFor(
    get(urlEqualTo("/fhdds/subscription/XEFH01234567890/get"))
      .willReturn(
        ok(Json.toJson(mkBusinessPartnerRecord)("sole").toString())
    ))
    builder
  }


}
