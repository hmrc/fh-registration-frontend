package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.forms.models.EoriNumber
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class ImportingActivitiesControllerISpec
  extends Specifications with TestConfiguration {
  val requestUrl = "importingActivities"

  "GET /importingActivities" when {
    "render the importing activities page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("Does the business have an EORI (Economic Operator Registration and Identification) number?")
            page.getElementsByTag("h1").text must include("Does the business have an EORI (Economic Operator Registration and Identification) number?")
          }
        }
      }
    }
  }

  "POST /importingActivities" when {
    "the user selects no" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "hasEori" -> Seq("false")
              ))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Form submitted, with result: ImportingActivities(false,None)")
            }
          }
        }
      }
    }

    "the user selects yes, supplies EORI number and selects false for goodsImportedOutsideEori" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "hasEori" -> Seq("true"),
                "eoriNumber.eoriNumber" -> Seq("GB1234567800"),
                "eoriNumber.goodsImportedOutsideEori" -> Seq("false")
              ))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Form submitted, with result: ImportingActivities(true,Some(EoriNumber(GB1234567800,false)))")
            }
          }
        }
      }
    }

    "the user selects yes, supplies EORI number and selects true for goodsImportedOutsideEori" should {
      "return 200" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "hasEori" -> Seq("true"),
                "eoriNumber.eoriNumber" -> Seq("GB1234567800"),
                "eoriNumber.goodsImportedOutsideEori" -> Seq("true")
              ))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Form submitted, with result: ImportingActivities(true,Some(EoriNumber(GB1234567800,true)))")
            }
          }
        }
      }
    }

    "the user selects yes and doesn't supply an EORI number" should {
      "return 400" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "hasEori" -> Seq("true"),
                "eoriNumber.eoriNumber" -> Seq.empty,
                "eoriNumber.goodsImportedOutsideEori" -> Seq("true")
              ))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the EORI number")
            }
          }
        }
      }
    }

    "the user selects yes, enters EORI and doesn't select option for goodsImportedOutsideEori" should {
      "return 400" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "hasEori" -> Seq("true"),
                "eoriNumber.eoriNumber" -> Seq("GB1234567800"),
                "eoriNumber.goodsImportedOutsideEori" -> Seq.empty
              ))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Select whether the business imports goods not belonging to it under its EORI number")
            }
          }
        }
      }
    }

    "the user selects yes and leaves EORI number form blank" should {
      "return 400" when {
        "the user is authenticated" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "hasEori" -> Seq("true"),
                "eoriNumber.eoriNumber" -> Seq.empty,
                "eoriNumber.goodsImportedOutsideEori" -> Seq.empty
              ))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Select whether the business imports goods not belonging to it under its EORI number")
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the EORI number")
            }
          }
        }
      }
    }
  }
}