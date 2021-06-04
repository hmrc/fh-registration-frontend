package uk.gov.hmrc.fhregistrationfrontend.testsupport.actions

import play.api.libs.ws.{WSClient, WSResponse}

import scala.concurrent.Future

class GetCalls(baseUrl: String) {

  def summaryPage(implicit client: WSClient): Future[WSResponse] = {
    client.url(s"$baseUrl/summary")
      .withFollowRedirects(false)
      .withHttpHeaders("X-Session-ID" → "some-id")
      .get()
  }

  def mainBusinessAddressPage(implicit client: WSClient): Future[WSResponse] = {
    client.url(s"$baseUrl/form/mainBusinessAddress")
      .withFollowRedirects(false)
      .withHttpHeaders("X-Session-ID" → "some-id")
      .get()
  }

  def contactPersonPage(implicit client: WSClient): Future[WSResponse] = {
    client.url(s"$baseUrl/form/contactPerson")
      .withFollowRedirects(false)
      .withHttpHeaders("X-Session-ID" → "some-id")
      .get()
  }

  def declarationPage(implicit client: WSClient): Future[WSResponse] = {
    client.url(s"$baseUrl/declaration")
      .withFollowRedirects(false)
      .withHttpHeaders("X-Session-ID" → "some-id")
      .get()
  }



}
