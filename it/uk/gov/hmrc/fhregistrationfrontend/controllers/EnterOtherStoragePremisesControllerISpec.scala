package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class EnterOtherStoragePremisesControllerISpec
  extends Specifications with TestConfiguration {
  val requestUrl = "otherStoragePremises/1"

  "GET /otherStoragePremises/1" when {

    "render the Other Storage Premises page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/$requestUrl")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("Other storage premises - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK")
          }
        }
      }
    }

  }

  "POST /otherStoragePremises/1" when {

    "the user is authenticated" should {
      "return 200" when {
        "The form is filled out correctly" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
            "storagePremise_address.Line1" -> Seq("44 Test town"),
                "storagePremise_address.Line2" -> Seq("Testville"),
                "storagePremise_address.Line3" -> Seq("Testington"),
                "storagePremise_address.Line4" -> Seq("Testland"),
            "storagePremise_address.postcode" -> Seq("AB1 2YZ"),
            "isThirdParty" -> Seq("true"),
            "addMore" -> Seq("false")))

            whenReady(result) { res =>
              res.status mustBe 200
              res.body must include("Form submitted with: (StoragePremise(Address(44 Test town,Some(Testville),Some(Testington),Some(Testland),AB1 2YZ,None,None),true),false)")
            }
          }
        }
      }

      "return 400" when {
        "Address line 1 is missing" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "storagePremise_address.Line1" -> Seq(""),
                "storagePremise_address.postcode" -> Seq("AB1 2YZ"),
                "isThirdParty" -> Seq("true"),
                "addMore" -> Seq("false")))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter address line 1")
              page.title must include("Other storage premises - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK")
            }
          }
        }
        "Postcode is missing" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "storagePremise_address.Line1" -> Seq("42 test street"),
                "storagePremise_address.postcode" -> Seq(""),
                "isThirdParty" -> Seq("true"),
                "addMore" -> Seq("false")))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the postcode of the address")
              page.title must include("Other storage premises - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK")
            }
          }
        }
        "Postcode is invalid" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "storagePremise_address.Line1" -> Seq("42 test street"),
                "storagePremise_address.postcode" -> Seq("invalid"),
                "isThirdParty" -> Seq("true"),
                "addMore" -> Seq("false")))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter a valid postcode")
              page.title must include("Other storage premises - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK")
            }
          }
        }
        "Third party radio button unchecked" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "storagePremise_address.Line1" -> Seq("42 test street"),
                "storagePremise_address.postcode" -> Seq("AB1 2YZ"),
                "isThirdParty" -> Seq(""),
                "addMore" -> Seq("false")))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Select whether these premises are operated by a third party")
              page.title must include("Other storage premises - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK")
            }
          }
        }
        "Are there anymore premises radio button unchecked" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/$requestUrl")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "storagePremise_address.Line1" -> Seq("42 test street"),
                "storagePremise_address.postcode" -> Seq("AB1 2YZ"),
                "isThirdParty" -> Seq("false")))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Select whether there are any more to add")
              page.title must include("Other storage premises - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK")
            }
          }
        }
      }
    }
  }

}