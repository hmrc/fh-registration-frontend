package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersCheckYourAnswersControllerISpec
  extends Specifications with TestConfiguration {

  val route = "/business-partners/check-your-answers"
  lazy val mockAppConfig = mock[FrontendAppConfig]

  s"GET $route" when {
    "the new business partners flow is enabled" should {
      "render the Check Your Answers page" when {
        "the user is authenticated" in {
          given.commonPrecondition
          when(mockAppConfig.getRandomBusinessType()).thenReturn("individual")

          WsTestClient.withClient { client =>
            val result = client.url(baseUrl + route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("Check your answers")
              page.body().text() must include("Type of partner Individual")
              page.body().text() must include("First name first name")
              page.body().text() must include("Last name last name")
              page.body().text() must include("National Insurance number QQ123456C")
              page.body().text() must include(" Address 1 Romford Road Wellington Telford TF1 4ER")
            }
          }
        }
      }
    }
  }

  s"POST $route" when {

    "the user clicks save and continue" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(baseUrl + route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "mock" -> Seq("true"),
              ))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Form submitted, with result:")
            }
          }
        }
      }
    }
  }
}