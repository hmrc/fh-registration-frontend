package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.http.HeaderNames
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerControllerIntegrationSpec
  extends Specifications with TestConfiguration {


  "GET /form/business-partner" when {

    "render the business partners page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        WsTestClient.withClient { client =>
          val result = client.url(s"$baseUrl/business-partners")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("Business partners - Apply for the Fulfilment House Due Diligence Scheme")
          }
        }
      }
    }

  }

  "POST /business-partners" when {

    "the user clicks save and continue" should {
      "redirect to the correct page" when {
        "the user is authenticated and select Individual" in {
          given.commonPrecondition

          val result = buildRequest("/business-partners")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "businessType" -> Seq("Individual"),
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/business-partners/partner-name")
          }
        }

        "the user is authenticated and select SoleProprietor " in {
          given.commonPrecondition

          val result = buildRequest("/business-partners")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "businessType" -> Seq("SoleProprietor"),
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/business-partners/partner-name")
          }
        }

        "the user is authenticated and select Partnership " in {
          given.commonPrecondition


          val result = buildRequest("/business-partners")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "businessType" -> Seq("Partnership"),
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/partnership-name")
          }
        }

        "the user is authenticated and select Limited Liability Partnership " in {
          given.commonPrecondition

          val result = buildRequest("/business-partners")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "businessType" -> Seq("LimitedLiabilityPartnership"),
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/limited-liability-partnership-name")
          }
        }

        "the user is authenticated and select Corporate Body " in {
          given.commonPrecondition

          val result = buildRequest("/business-partners")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "businessType" -> Seq("CorporateBody"),
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/company-name")
          }
        }

        "the user is authenticated and select Unincorporated Body " in {
          given.commonPrecondition

          val result = buildRequest("/business-partners")
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "businessType" -> Seq("UnincorporatedBody"),
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some("/fhdds/form/business-partners/unincorporated-body-name")
          }
        }
      }

      "return 400" when {
        "the user is authenticated and Nothing is selected" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl/business-partners")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "" -> Seq(""),
              ))

            whenReady(result) { res =>
              res.status mustBe 400
              res.body must include("Select the type of legal entity for the partner")
            }
          }
        }
      }
    }
  }
}
