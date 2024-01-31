package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{Mode, CheckMode, NormalMode}
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersPartnershipConfirmRegisteredAddressControllerISpec
  extends Specifications with TestConfiguration {

  val index = 1
  val checkYourAnswersPageUrl: String = routes.BusinessPartnersCheckYourAnswersController.load().url

  List(NormalMode, CheckMode).foreach { mode =>
    val route = routes.BusinessPartnersPartnershipConfirmRegisteredAddressController.load(index, mode).url.drop(6)
    val registeredAddressPageUrl: String = routes.BusinessPartnersPartnershipRegisteredAddressController.load(index, mode).url

    s"GET $route" when {
      "render the business partner confirm address page" when {
        "when UkAddressLookup contains a single address" in {
          given.commonPrecondition
          addUserAnswersToSession(seedCacheWithUKAddressLookup(singleAddress))

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include("Confirm the partnership’s registered office address?")
            page.getElementsByClass("govuk-heading-l").text() must include("Confirm the company’s registered office address")
            page.getElementsByClass("govuk-body-m").text() must include("1 Romford Road")
          }
        }
      }

      "redirect to the Registered Address page" when {
        "when UkAddressLookup contains multiple addresses" in {
          given.commonPrecondition
          addUserAnswersToSession(seedCacheWithUKAddressLookup(multipleAddresses))

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(registeredAddressPageUrl)
          }
        }

        "when UkAddressLookup contains an empty address list" in {
          given.commonPrecondition
          addUserAnswersToSession(seedCacheWithUKAddressLookup(Map.empty))

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(registeredAddressPageUrl)
          }
        }
      }

      "redirect to the start of BusinessPartners" when {
        "there are no user answers in the database" in {
          given.commonPrecondition

          val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersController.load(index, mode).url)
          }
        }
      }
    }

    s"POST $route" when {
      "the user clicks save and continue" should {
        "Redirect to the CYA page" when {
          "UkAddressLookup has a single Addreee" in {
            given.commonPrecondition
            addUserAnswersToSession(seedCacheWithUKAddressLookup(singleAddress))

            val result = buildRequest(route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "mock" -> Seq("true"),
              ))

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(checkYourAnswersPageUrl)
            }
          }
        }

        "redirect to the Registered Address page" when {
          "UkAddressLookup has multiple Addresses" in {
            given.commonPrecondition
            addUserAnswersToSession(seedCacheWithUKAddressLookup(multipleAddresses))

            val result = buildRequest(route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "mock" -> Seq("true"),
              ))

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(registeredAddressPageUrl)
            }
          }

          "when UkAddressLookup contains an empty address list" in {
            given.commonPrecondition
            addUserAnswersToSession(seedCacheWithUKAddressLookup(Map.empty))

            val result = buildRequest(route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "mock" -> Seq("true"),
              ))

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(registeredAddressPageUrl)
            }
          }
        }
      }
    }
  }
}
