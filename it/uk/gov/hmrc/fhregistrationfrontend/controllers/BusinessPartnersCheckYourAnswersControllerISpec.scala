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

  lazy val mockAppConfig = mock[FrontendAppConfig]
  val route = "/business-partners/check-your-answers"

  s"GET $route" when {
    "the new business partners flow is enabled" should {
      "render the Check Your Answers page" when {
        "the business partnerType is an individual" in {
          given.commonPrecondition

          WsTestClient.withClient { client =>
            val result = client.url(s"$baseUrl$route?partnerType=individual")
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("Check your answers")
              page.getElementsByClass("govuk-summary-list__key").text() must include("Type of partner")
              page.getElementsByClass("govuk-summary-list__value").text() must include("Individual")
              page.getElementsByClass("govuk-summary-list__key").text() must include("First name")
              page.getElementsByClass("govuk-summary-list__value").text() must include("first name")
              page.getElementsByClass("govuk-summary-list__key").text() must include("National Insurance number")
              page.getElementsByClass("govuk-summary-list__value").text() must include("QQ123456C")
              page.getElementsByClass("govuk-summary-list__key").text() must include("Address")
              page.getElementsByClass("govuk-summary-list__value").text() must include("1 Romford Road Wellington Telford TF1 4ER")
            }
          }
        }

        "the business partnerType is an limited liability partnership" when {
          "No trading name and VAT number provided" in {
            given.commonPrecondition

            WsTestClient.withClient { client =>
              val result = client.url(s"$baseUrl$route?partnerType=limited-liability-partnership")
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .get()

              whenReady(result) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title() must include("Check your answers")
                page.getElementsByClass("govuk-summary-list__key").text() must include("Type of partner")
                page.getElementsByClass("govuk-summary-list__value").text() must include("Limited Liability Partnership")
                page.getElementsByClass("govuk-summary-list__key").text() must include("Name of partnership")
                page.getElementsByClass("govuk-summary-list__value").text() must include("llp trading name")
                page.getElementsByClass("govuk-summary-list__key").text() must include("Trading name")
                page.getElementsByClass("govuk-summary-list__value").text() must include("None")
                page.getElementsByClass("govuk-summary-list__key").text() must include("Company registration number")
                page.getElementsByClass("govuk-summary-list__value").text() must include("01234567")
                page.getElementsByClass("govuk-summary-list__key").text() must include("VAT registration number")
                page.getElementsByClass("govuk-summary-list__value").text() must include("None")
                page.getElementsByClass("govuk-summary-list__key").text() must include("Corporation Tax Unique Taxpayer Reference (UTR)")
                page.getElementsByClass("govuk-summary-list__value").text() must include("1234567890")
                page.getElementsByClass("govuk-summary-list__key").text() must include("Address")
                page.getElementsByClass("govuk-summary-list__value").text() must include("1 Romford Road Wellington Telford TF1 4ER")
              }
            }
          }

          "trading name and VAT number provided" in {
            given.commonPrecondition

            WsTestClient.withClient { client =>
              val result = client.url(s"$baseUrl$route?partnerType=limited-liability-partnership-with-vat-and-trading-name")
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .get()

              whenReady(result) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title() must include("Check your answers")
                page.getElementsByClass("govuk-summary-list__key").text() must include("Type of partner")
                page.getElementsByClass("govuk-summary-list__value").text() must include("Limited Liability Partnership")
                page.getElementsByClass("govuk-summary-list__key").text() must include("Name of partnership")
                page.getElementsByClass("govuk-summary-list__value").text() must include("llp trading name")
                page.getElementsByClass("govuk-summary-list__key").text() must include("Trading name")
                page.getElementsByClass("govuk-summary-list__value").text() must include("trade partner name")
                page.getElementsByClass("govuk-summary-list__key").text() must include("Company registration number")
                page.getElementsByClass("govuk-summary-list__value").text() must include("01234567")
                page.getElementsByClass("govuk-summary-list__key").text() must include("VAT registration number")
                page.getElementsByClass("govuk-summary-list__value").text() must include("123456789")
                page.getElementsByClass("govuk-summary-list__key").text() must include("Address")
                page.getElementsByClass("govuk-summary-list__value").text() must include("1 Romford Road Wellington Telford TF1 4ER")
              }
            }
          }
        }

        "the business partnerType is a partnership" when {
            "optional values are included" in {
                given.commonPrecondition

                WsTestClient.withClient { client =>
                    val result = client.url(s"$baseUrl$route?partnerType=partnership-with-optional-values")
                      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                      .get()

                    whenReady(result) { res =>
                        res.status mustBe 200
                        val page = Jsoup.parse(res.body)
                        page.title() must include("Check your answers")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Type of partner")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("Partnership")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Name of partnership")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("partnership name")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Trading name")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("partnership trading name")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("VAT registration number")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("123456789")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Self Assessment Unique Taxpayer Reference (UTR)")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("1234567890")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Address")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("1 Romford Road Wellington Telford TF1 4ER")
                    }
                }
            }

            "optional values are none" in {
                given.commonPrecondition

                WsTestClient.withClient { client =>
                    val result = client.url(s"$baseUrl$route?partnerType=partnership")
                      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                      .get()

                    whenReady(result) { res =>
                        res.status mustBe 200
                        val page = Jsoup.parse(res.body)
                        page.title() must include("Check your answers")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Type of partner")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("Partnership")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Name of partnership")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("partnership name")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Trading name")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("None")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("VAT registration number")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("None")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Self Assessment Unique Taxpayer Reference (UTR)")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("None")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Address")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("1 Romford Road Wellington Telford TF1 4ER")
                    }
                }
            }
        }

        "the business partnerType is a Sole Proprietor" when {
            "No VAT number provided" in {
                given.commonPrecondition

                WsTestClient.withClient { client =>
                    val result = client.url(s"$baseUrl$route?partnerType=sole-proprietor")
                      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                      .get()

                    whenReady(result) { res =>
                        res.status mustBe 200
                        val page = Jsoup.parse(res.body)
                        page.title() must include("Check your answers")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Type of partner")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("Sole Proprietor")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("First name")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("Bob")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Last name")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("Testman")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Trading name")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("Trade Name Ltd")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("National Insurance number")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("AB123456C")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("VAT registration number")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("None")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Self Assessment Unique Taxpayer Reference (UTR)")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("1234567890")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Address")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("1 Romford Road Wellington Telford TF1 4ER")
                    }
                }
            }

            "VAT number provided" in {
                given.commonPrecondition

                WsTestClient.withClient { client =>
                    val result = client.url(s"$baseUrl$route?partnerType=sole-proprietor-with-vat")
                      .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                      .get()

                    whenReady(result) { res =>
                        res.status mustBe 200
                        val page = Jsoup.parse(res.body)
                        page.title() must include("Check your answers")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Type of partner")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("Sole Proprietor")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("First name")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("Bob")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Last name")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("Testman")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Trading name")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("Trade Name Ltd")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("National Insurance number")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("AB123456C")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("VAT registration number")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("123456789")
                        page.getElementsByClass("govuk-summary-list__key").text() mustNot include("Self Assessment Unique Taxpayer Reference (UTR)")
                        page.getElementsByClass("govuk-summary-list__value").text() mustNot include("1234567890")
                        page.getElementsByClass("govuk-summary-list__key").text() must include("Address")
                        page.getElementsByClass("govuk-summary-list__value").text() must include("1 Romford Road Wellington Telford TF1 4ER")
                    }
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