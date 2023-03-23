package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerAddressControllerIntegrationSpec
  extends Specifications with TestConfiguration {

  "GET /form/business-partners/partner-address" when {

    "the new business partners flow is enabled" should {

      "render the business partner address page" when {
        "the user is authenticated" in {
          given
            .commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/form/business-partners/partner-address")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("What is the partner’s address?")
              page.getElementById("page-heading").text() must include("What is Test User’s address?")
            }
          }
        }
      }
    }


  }

  "POST /form/business-partners/partner-address" when {
    "the form has no errors" should {
      "return 200" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partner-address")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq("1"),
                      "partnerPostcode"    -> Seq("AB1 2YZ")))

          whenReady(result) { res =>
            res.status mustBe 200
          }
        }
      }
    }

    "address line not populated" should {
      "return 200" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partner-address")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq.empty,
                      "partnerPostcode"    -> Seq("AB1 2YZ")))

          whenReady(result) { res =>
            res.status mustBe 200
          }
        }
      }
    }

    "postcode not populated" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partner-address")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq("1"),
                      "partnerPostcode"    -> Seq.empty))

          whenReady(result) { res =>
            res.status mustBe 400
          }
        }
      }
    }

    "postcode invalid format" should {
      "return 400" in {
        given
          .commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/form/business-partners/partner-address")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq("1"),
                      "partnerPostcode"    -> Seq("A")))

          whenReady(result) { res =>
            res.status mustBe 400
          }
        }
      }
    }
  }


}
    /*"Show the summary page when the user has fulfilled all the pages" in {

      given
        .summaryPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/summary")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
        }

      }
    }

    "Show the bad request page when the user has not fulfilled all the pages" in {
      given
        .commonPrecondition

      WsTestClient.withClient { client =>
        val result = client.url(s"$baseUrl/summary")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 400
        }

      }
    }
  }

}*/
