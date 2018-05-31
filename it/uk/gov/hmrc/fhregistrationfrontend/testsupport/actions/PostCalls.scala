package uk.gov.hmrc.fhregistrationfrontend.testsupport.actions


import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.Future

class PostCalls(baseUrl: String)(implicit builder: ActionsBuilder) {

  def declaration(implicit client: WSClient): Future[WSResponse] = {
    client.url(s"$baseUrl/submit")
      .withFollowRedirects(false)
      .withHeaders("X-Session-ID" → "some-id",
        "Csrf-Token" -> "nocheck",
        "Content-Type" → "application/json")
      .post(declarationData)
  }

  private def declarationData = {
    """{
      |"fullName": "John",
      |"jobTitle": "Director",
      |"isUseGgEmail": true,
      |"ggEmail": "user@test.com"
      |}
    """.stripMargin
  }

}
