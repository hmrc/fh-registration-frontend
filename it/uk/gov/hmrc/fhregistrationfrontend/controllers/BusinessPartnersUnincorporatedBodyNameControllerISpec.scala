package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.NormalMode
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersUnincorporatedBodyNameControllerISpec
  extends Specifications with TestConfiguration {

  val route: String = routes.BusinessPartnersUnincorporatedBodyNameController.load(index = 1, NormalMode).url.drop(6)
  val unincorpBodyTradingNameUrl: String = routes.BusinessPartnersUnincorporatedBodyTradingNameController.load().url

  s"GET $route" should {
    "render the Unincorporated Body Name page" in {
      given
        .commonPrecondition

      val result = buildRequest(route)
        .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
        .get()

      whenReady(result) { res =>
        res.status mustBe 200
        val page = Jsoup.parse(res.body)
        page.title() must include("What is the unincorporated body name?")
        page.getElementsByTag("h1").text() must include("What is the unincorporated body name?")
      }
    }
  }

  s"POST $route" when {
    "the unincorporated body name is entered" should {
      "redirect to the Unincorporated Body Trading Name page" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map(
            "unincorporatedBodyName_value" -> Seq("Test Body")
          ))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(unincorpBodyTradingNameUrl)
        }
      }
    }

    "Unincorporated body name is not supplied" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("unincorporatedBodyName_value" -> Seq("")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter an unincorporated body name")
        }
      }
    }

    "Unincorporated body name is not valid (exceeds 120 chars)" should {
      "return 400" in {
        given
          .commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
          .post(Map("unincorporatedBodyName_value" -> Seq("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Unincorporated body name must be 120 characters or less")
        }
      }
    }
  }
}
