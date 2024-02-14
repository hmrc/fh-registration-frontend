package uk.gov.hmrc.fhregistrationfrontend.controllers.businessPartners

import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersChooseAddressControllerISpec extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersChooseAddressController.load(1, mode).url.drop(6)

  val index: Int = 1

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" should {
      "render the choose address page" in {
        given.commonPrecondition
        addUserAnswersToSession(seedCacheWithUKAddressLookup(multipleAddresses))

        val result = buildRequest(route(mode))
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("Choose address")
          val addressesListed = page.getElementsByClass("govuk-radios__label").text()
          addressesListed must include("1 Romford Road, Wellington, Telford, TF1 4ER")
        }
      }

      "redirect to the start" when {
        "there are no user answers in the database" in {
          given.commonPrecondition

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(startCall.url)
          }
        }
      }
    }

    s"POST ${route(mode)}" should {
      "redirect the user to the Check Your Answers page" when {
        "the form has no errors and the first value is selected" in {
          given.commonPrecondition
          addUserAnswersToSession(seedCacheWithUKAddressLookup(multipleAddresses))

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "chosenAddress" -> Seq("1")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersCheckYourAnswersController.load().url)
          }
        }

        "the form has no errors and the second value is selected" in {
          given.commonPrecondition
          addUserAnswersToSession(seedCacheWithUKAddressLookup(multipleAddresses))

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "chosenAddress" -> Seq("2")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersCheckYourAnswersController.load().url)
          }
        }
      }

      "redirect the user to the Business Partners Address page" when {
        "addressList cache contains no addresses" in {
          given.commonPrecondition
          addUserAnswersToSession(seedCacheWithUKAddressLookup(Map.empty))

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "chosenAddress" -> Seq("1")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersAddressController.load(index, mode).url)
          }
        }

        "addressList cache contains a single addresses" in {
          given.commonPrecondition
          addUserAnswersToSession(seedCacheWithUKAddressLookup(singleAddress))

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(
              "chosenAddress" -> Seq("2")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersAddressController.load(index, mode).url)
          }
        }
      }
    }
  }
}
