package uk.gov.hmrc.fhregistrationfrontend.controllers.companyOfficers

import org.jsoup.Jsoup
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

import java.time.LocalDate


class CompanyOfficersCYAControllerISpec extends Specifications with TestConfiguration {

  lazy val mockAppConfig = mock[FrontendAppConfig]
  val route = "/company-officers/check-your-answers/1"

  "GET " + route when {
    "the userAnswers exist" should {
      "render the check your answers page" in {
        val userAnswers = emptyUserAnswers
        given
          .commonPrecondition

        addUserAnswersToSession(userAnswers)

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

          whenReady(result) { res =>
            res.status mustBe OK
            val page = Jsoup.parse(res.body)
            page.title must include("Check your answers")
          }
        }
      }
    }


  s"POST $route" when {

    "the user clicks save and continue" should {
      "return 200" when {
        "the user has user answers" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

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
