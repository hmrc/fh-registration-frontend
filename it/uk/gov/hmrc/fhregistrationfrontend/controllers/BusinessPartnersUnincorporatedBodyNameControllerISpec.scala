package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersUnincorporatedBodyNameControllerISpec
  extends Specifications with TestConfiguration {

  "GET /form/business-partners/unincorporated-body-name" should {
    "render the Unincorporated Body Name page" in {
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/form/business-partners/unincorporated-body-name")
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
  }

  "POST /form/business-partners/unincorporated-body-name" when {
    "the unincorporated body name is entered" should {
      "return 200 with companyRegistrationNumber" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/unincorporated-body-name")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("unincorporatedBodyName_value" -> Seq("Test Body")))

          whenReady(result) { res =>
            res.status mustBe 200
            res.body mustBe "Form submitted, with result: UnincorporatedBodyName(Test Body)"
          }
        }
      }
    }

    "Unincorporated body name is not supplied" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/unincorporated-body-name")
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
    }

    "Unincorporated body name is not valid (exceeds 120 chars)" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/unincorporated-body-name")
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
}
