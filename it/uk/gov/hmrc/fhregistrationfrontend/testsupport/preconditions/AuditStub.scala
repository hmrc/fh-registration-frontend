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
