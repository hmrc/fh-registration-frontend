/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.{Address, BusinessRegistrationDetails}
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json

case class BusinessCustomerFrontendStub()(implicit builder: PreconditionBuilder) {
  import BusinessRegistrationDetails.formats

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
