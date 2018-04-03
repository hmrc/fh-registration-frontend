package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

import com.github.tomakehurst.wiremock.client.WireMock._

case class UserStub
()
  (implicit builder: PreconditionBuilder) {


  def isAuthorised() = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(
          ok(
            s"""
               |{
               |  "internalId": "some-id",
               |  "email": "test@test.com",
               |  "allEnrolments": [],
               |  "loginTimes": {
               |     "currentLogin": "2018-03-27T09:00:00.000Z",
               |     "previousLogin": "2018-03-01T12:00:00.000Z"
               |  }
               |}
             """.stripMargin
          )
        )
    )
    builder

  }

  def isNotAuthorised(reason: String = "MissingBearerToken") = {
    stubFor(
      post(urlPathEqualTo("/auth/authorise"))
        .willReturn(unauthorized().withHeader("WWW-Authenticate", s"""MDTP detail="$reason"""")))

    builder
  }

}