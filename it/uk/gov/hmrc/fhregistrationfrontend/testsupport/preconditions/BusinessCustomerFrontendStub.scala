package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.{Address, BusinessRegistrationDetails}
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json

case class BusinessCustomerFrontendStub()
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

  private def mkBusinessPartnerRecord() = {
    BusinessRegistrationDetails(businessName = "Real Business Inc",
      businessType = Some("corporate body"),
      businessAddress = aFakeAddress,
      sapNumber = "1234567890",
      safeId = "XE0001234567890",
      agentReferenceNumber = Some("JARN1234567"),
      firstName = None,
      lastName = None,
      utr = Some("1111111111"),
      identification = None)
  }

  def hasBusinessPartnerRecord() = {
    stubFor(
      get(urlPathEqualTo("/business-customer/fetch-review-details/FHDDS"))
      .willReturn(ok(
        Json.toJson(mkBusinessPartnerRecord()).toString()
      ))
    )
    builder
  }
}
