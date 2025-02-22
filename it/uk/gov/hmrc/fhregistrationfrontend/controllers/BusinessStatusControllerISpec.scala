package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.api.libs.ws.writeableOf_urlEncodedForm
import play.api.libs.ws.DefaultBodyReadables.*

class BusinessStatusControllerISpec extends Specifications with TestConfiguration  {
  val requestUrl = "businessStatus"

  "GET /businessStatus" when {

    "render the business status page" when {
      "the user is authenticated" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("Are you setting up a new fulfilment business?")
          }
        }
      }
    }

  }

  "POST /businessStatus" when {

    "Yes radio button selected and details are entered" should {
      "return 200" when {
        "the user is authenticated" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "isNewFulfilmentBusiness" -> Seq("true"),
                  "proposedStartDate.day"   -> Seq("01"),
                  "proposedStartDate.month" -> Seq("01"),
                  "proposedStartDate.year"  -> Seq("2020")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 200
              res.body[String] must include("Form submitted, with result: BusinessStatus(true,Some(2020-01-01))")
            }
          }
        }

        "No radio button is selected" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "isNewFulfilmentBusiness" -> Seq("false")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 200
              res.body[String] must include("Form submitted, with result: BusinessStatus(false,None)")
            }
          }
        }
      }
    }

    "a radio button isn't selected" should {
      "return 400" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(
              Map(
                "isNewFulfilmentBusiness" -> Seq.empty
              )
            )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Are you setting up a new fulfilment business?")
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
              "Select whether the business is setting up a new fulfilment business"
            )
          }
        }
      }
    }

    "Yes radio button is selected but no date is entered" should {
      "return 400" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(
              Map(
                "isNewFulfilmentBusiness" -> Seq("true"),
                "proposedStartDate.day"   -> Seq(""),
                "proposedStartDate.month" -> Seq(""),
                "proposedStartDate.year"  -> Seq("")
              )
            )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Are you setting up a new fulfilment business?")
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
              "Enter the intended start date"
            )
          }
        }
      }
    }

    "Yes radio button is selected but an invalid date is entered" should {
      "return 400" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(
              Map(
                "isNewFulfilmentBusiness" -> Seq("true"),
                "proposedStartDate.day"   -> Seq("ab"),
                "proposedStartDate.month" -> Seq("cd"),
                "proposedStartDate.year"  -> Seq("ef")
              )
            )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Are you setting up a new fulfilment business?")
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
              "Enter a valid date for the intended start date"
            )
          }
        }
      }
    }
  }

}
