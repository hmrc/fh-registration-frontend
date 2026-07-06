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
import com.github.tomakehurst.wiremock.client.WireMock._

case class AuditStub()(implicit builder: PreconditionBuilder) {
  def writesAudit(status: Int = 200) = {
    stubFor(
      post(urlMatching("/write/audit"))
        .willReturn(
          aResponse().withStatus(status).withBody("""{"x":2}""")
        )
    )
    builder
  }

  def writesAuditMerged(status: Int = 200) = {
    stubFor(
      post(urlMatching("/write/audit/merged"))
        .willReturn(
          aResponse().withStatus(status).withBody("""{"x":2}""")
        )
    )
    builder
  }

  def writesAuditOrMerged(status: Int = 200) = {
    writesAuditMerged(status)
    writesAudit(status)
  }

  def failsToWriteAudit() =
    writesAudit(404)
}
