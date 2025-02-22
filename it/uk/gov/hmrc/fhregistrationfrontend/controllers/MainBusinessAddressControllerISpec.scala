package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import play.api.libs.ws.DefaultBodyWritables.writeableOf_urlEncodedForm
import play.api.libs.ws.DefaultBodyReadables.*

class MainBusinessAddressControllerISpec
    extends Specifications with TestConfiguration{
  val requestUrl = "mainBusinessAddress"
  val pageTitle = "Business address - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK"

  "GET /mainBusinessAddress" when {

    "render the main business address" when {
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
            page.title must include(pageTitle)
          }
        }
      }
    }
  }

  "POST /mainBusinessAddress" when {

    "the user selects Less than 3 years" should {
      "return 200" when {
        "the user selects no" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "timeAtCurrentAddress" -> Seq("Less than 3 years"),
                  "previousAddress"      -> Seq("false")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 200
              res.body[String] must include(
                "Form submitted, with result:MainBusinessAddress(Less than 3 years,Some(false),None,None)"
              )
            }
          }
        }

        "the user selects yes and correctly fills out the form" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "timeAtCurrentAddress"                           -> Seq("Less than 3 years"),
                  "previousAddress"                                -> Seq("true"),
                  "mainPreviousAddressUK_previousAddress.Line1"    -> Seq("line 1"),
                  "mainPreviousAddressUK_previousAddress.postcode" -> Seq("AA1 1YZ"),
                  "previousAddressStartdate.day"                   -> Seq("01"),
                  "previousAddressStartdate.month"                 -> Seq("10"),
                  "previousAddressStartdate.year"                  -> Seq("2022")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 200
              res.body[String] must include(
                "Form submitted, with result:MainBusinessAddress(Less than 3 years,Some(true),Some(Address(line 1,None,None,None,AA1 1YZ,None,None)),Some(2022-10-01))"
              )
            }
          }
        }
      }

      "return 400" when {
        "the user selects yes, leaves address line 1 blank" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "timeAtCurrentAddress"                           -> Seq("Less than 3 years"),
                  "previousAddress"                                -> Seq("true"),
                  "mainPreviousAddressUK_previousAddress.Line1"    -> Seq(""),
                  "mainPreviousAddressUK_previousAddress.postcode" -> Seq("AA1 1AA"),
                  "previousAddressStartdate.day"                   -> Seq("01"),
                  "previousAddressStartdate.month"                 -> Seq("10"),
                  "previousAddressStartdate.year"                  -> Seq("2022")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must
                include("You must enter line 1 of the address")
            }
          }
        }
      }

      "return 400" when {
        "the user selects yes, leaves postcode field blank" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "timeAtCurrentAddress"                           -> Seq("Less than 3 years"),
                  "previousAddress"                                -> Seq("true"),
                  "mainPreviousAddressUK_previousAddress.Line1"    -> Seq("Line 1"),
                  "mainPreviousAddressUK_previousAddress.postcode" -> Seq(""),
                  "previousAddressStartdate.day"                   -> Seq("01"),
                  "previousAddressStartdate.month"                 -> Seq("10"),
                  "previousAddressStartdate.year"                  -> Seq("2022")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must
                include("Enter the postcode of the address")
            }
          }
        }
      }

      "return 400" when {
        "the user selects yes, enters invalid postcode" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "timeAtCurrentAddress"                           -> Seq("Less than 3 years"),
                  "previousAddress"                                -> Seq("true"),
                  "mainPreviousAddressUK_previousAddress.Line1"    -> Seq("Line 1"),
                  "mainPreviousAddressUK_previousAddress.postcode" -> Seq("AAAAAAAA"),
                  "previousAddressStartdate.day"                   -> Seq("01"),
                  "previousAddressStartdate.month"                 -> Seq("10"),
                  "previousAddressStartdate.year"                  -> Seq("2022")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must
                include("Enter a postcode, like AA1 1AA")
            }
          }
        }
      }

      "return 400" when {
        "the user selects yes, leaves day field blank" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "timeAtCurrentAddress"                           -> Seq("Less than 3 years"),
                  "previousAddress"                                -> Seq("true"),
                  "mainPreviousAddressUK_previousAddress.Line1"    -> Seq("Line 1"),
                  "mainPreviousAddressUK_previousAddress.postcode" -> Seq("AAB 1XY"),
                  "previousAddressStartdate.day"                   -> Seq(""),
                  "previousAddressStartdate.month"                 -> Seq("10"),
                  "previousAddressStartdate.year"                  -> Seq("2022")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must
                include("Enter a day for the intended start date")
            }
          }
        }

        "the user selects yes, leaves month field blank" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "timeAtCurrentAddress"                           -> Seq("Less than 3 years"),
                  "previousAddress"                                -> Seq("true"),
                  "mainPreviousAddressUK_previousAddress.Line1"    -> Seq("Line 1"),
                  "mainPreviousAddressUK_previousAddress.postcode" -> Seq("AAB 1XY"),
                  "previousAddressStartdate.day"                   -> Seq("1"),
                  "previousAddressStartdate.month"                 -> Seq(""),
                  "previousAddressStartdate.year"                  -> Seq("2022")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must
                include("Enter a month for the intended start date")
            }
          }
        }

        "the user selects yes, leaves year field blank" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "timeAtCurrentAddress"                           -> Seq("Less than 3 years"),
                  "previousAddress"                                -> Seq("true"),
                  "mainPreviousAddressUK_previousAddress.Line1"    -> Seq("Line 1"),
                  "mainPreviousAddressUK_previousAddress.postcode" -> Seq("AAB 1XY"),
                  "previousAddressStartdate.day"                   -> Seq("1"),
                  "previousAddressStartdate.month"                 -> Seq("10"),
                  "previousAddressStartdate.year"                  -> Seq("")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must
                include("Enter a year for the intended start date")
            }
          }
        }

        "the user selects yes, leaves date field blank" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "timeAtCurrentAddress"                           -> Seq("Less than 3 years"),
                  "previousAddress"                                -> Seq("true"),
                  "mainPreviousAddressUK_previousAddress.Line1"    -> Seq("Line 1"),
                  "mainPreviousAddressUK_previousAddress.postcode" -> Seq("AAB 1XY"),
                  "previousAddressStartdate.day"                   -> Seq(""),
                  "previousAddressStartdate.month"                 -> Seq(""),
                  "previousAddressStartdate.year"                  -> Seq("")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must
                include("Enter the date when the business started operating from the previous address")
            }
          }
        }

        "the user selects yes, leaves all fields blank" in {
          `given`.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client
              .url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(
                Map(
                  "timeAtCurrentAddress"                           -> Seq("Less than 3 years"),
                  "previousAddress"                                -> Seq("true"),
                  "mainPreviousAddressUK_previousAddress.Line1"    -> Seq(""),
                  "mainPreviousAddressUK_previousAddress.postcode" -> Seq(""),
                  "previousAddressStartdate.day"                   -> Seq(""),
                  "previousAddressStartdate.month"                 -> Seq(""),
                  "previousAddressStartdate.year"                  -> Seq("")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must
                include("Enter the date when the business started operating from the previous address")
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must
                include("Enter the postcode of the address")
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must
                include("You must enter line 1 of the address")
            }
          }
        }
      }
    }

    "the user selects 3-5 years and submits the form" should {
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
                  "timeAtCurrentAddress" -> Seq("3 to 5 years")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 200
              res.body[String] must include(
                "Form submitted, with result:MainBusinessAddress(3 to 5 years,None,None,None)"
              )
            }
          }
        }
      }
    }

    "the user selects 5-10 years and submits the form" should {
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
                  "timeAtCurrentAddress" -> Seq("5 to 10 years")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 200
              res.body[String] must include(
                "Form submitted, with result:MainBusinessAddress(5 to 10 years,None,None,None)"
              )
            }
          }
        }
      }
    }

    "the user selects 10 or more years and submits the form" should {
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
                  "timeAtCurrentAddress" -> Seq("10 or more years")
                )
              )

            whenReady(result) { res =>
              res.status mustBe 200
              res.body[String] must include(
                "Form submitted, with result:MainBusinessAddress(10 or more years,None,None,None)"
              )
            }
          }
        }
      }
    }

    "no radio option is selected by the user" should {
      "return 400" in {
        `given`.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client
            .url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(
              Map(
                "timeAtCurrentAddress" -> Seq("")
              )
            )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include(pageTitle)
            page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
              "Select how long the business has operated from this address"
            )
          }
        }
      }
    }
  }

}
