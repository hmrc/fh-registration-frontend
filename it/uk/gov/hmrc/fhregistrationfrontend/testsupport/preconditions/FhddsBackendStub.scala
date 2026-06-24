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

import java.util.Date
import com.github.tomakehurst.wiremock.client.WireMock.*
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.fhregistration.models.fhdds.SubmissionResponse
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.{Address, BusinessRegistrationDetails}
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress

import scala.annotation.unused

case class FhddsBackendStub()(implicit builder: PreconditionBuilder) {
  import BusinessRegistrationDetails.formats

  private val aFakeAddress =
    Address(line1 = "line1", line2 = "line2", line3 = None, line4 = None, postcode = Some("NE98 1ZZ"), country = "GB")

  private def mkBusinessPartnerRecord(@unused businessType: String) =
    BusinessRegistrationDetails(
      businessName = Some("Real Business Inc"),
      None,
      businessAddress = aFakeAddress,
      safeId = Some("XE0001234567890"),
      utr = Some("1111111111")
    )

  def hasBusinessDetails(formType: String, businessType: String) = {
    stubFor(
      put(urlPathEqualTo(s"/fhdds/submission-extra-data/some-id/$formType/businessRegistrationDetails"))
        .willReturn(
          ok(
            Json.toJson(mkBusinessPartnerRecord(businessType)).toString()
          )
        )
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
      post(urlPathMatching("/fhdds/subscription/amend/..FH[0-9A-Z]+"))
        .willReturn(ok(Json.toJson(SubmissionResponse("", new Date)).toString()))
    )

    builder
  }

  def createSubscription() = {
    stubFor(
      post(urlPathMatching("/fhdds/subscription/subscribe/XE0001234567890"))
        .willReturn(ok(Json.toJson(SubmissionResponse("XEFH01234567890.", new Date)).toString()))
    )

    builder
  }

  val limitedCompanyJsonString = getSubmissionJson("fhdds-limited-company-minimum").toString()

  def getSubscription() = {
    stubFor(
      get(urlPathMatching("/fhdds/subscription/XEFH01234567890/get"))
        .willReturn(ok(limitedCompanyJsonString))
    )

    builder
  }

  private def getSubmissionJson(jsonFile: String): JsValue = {
    val resource = getClass.getResourceAsStream(s"/json/valid/display/limited-company/$jsonFile.json")
    Json.parse(resource).as[JsValue]
  }

}
