package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.{Address, BusinessRegistrationDetails}

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

}
