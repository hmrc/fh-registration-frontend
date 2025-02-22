package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.{Address, BusinessRegistrationDetails}
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json

case class BusinessCustomerFrontendStub()(implicit builder: PreconditionBuilder) {
  import BusinessRegistrationDetails.format

  private val aFakeAddress =
    Address(line1 = "line1", line2 = "line2", line3 = None, line4 = None, postcode = Some("NE98 1ZZ"), country = "GB")

  private def mkBusinessPartnerRecord =
    BusinessRegistrationDetails(
      businessName = Some("Real Business Inc"),
      None,
      businessAddress = aFakeAddress,
      safeId = Some("XE0001234567890"),
      utr = Some("1111111111")
    )

  def hasBusinessCustomerRecord = {
    stubFor(
      get(urlPathEqualTo("/business-customer/fetch-review-details/FHDDS"))
        .willReturn(
          ok(
            Json.toJson(mkBusinessPartnerRecord).toString()
          )
        )
    )
    builder
  }
}
