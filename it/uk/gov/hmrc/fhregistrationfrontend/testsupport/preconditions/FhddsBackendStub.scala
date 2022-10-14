package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import java.util.Date
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json
import uk.gov.hmrc.fhregistration.models.fhdds.{SubmissionRequest, SubmissionResponse}
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.{Address, BusinessRegistrationDetails}
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.{EnrolmentProgress, FhddsStatus}
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus.FhddsStatus
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

  private val FhddsStatus =
    """
      |{
      |  FhddsStatus : "Processing",
      |}
  """.stripMargin

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
        ok(Json.toJson(FhddsStatus, "XE0001234567890").toString())
    ))
    builder
  }


}
