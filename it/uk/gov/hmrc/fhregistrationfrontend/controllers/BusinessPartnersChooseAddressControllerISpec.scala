package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, UkAddressLookup}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.UkAddressLookupPage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersChooseAddressControllerISpec extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersChooseAddressController.load(1, mode).url.drop(6)

  val index: Int = 1

  private val testAddressData: Map[String, Address] = {
    val address1 = Address(
      addressLine1 = "1 Romford Road",
      addressLine2 = Some("Wellington"),
      addressLine3 = Some("Telford"),
      addressLine4 = None,
      postcode = "TF1 4ER",
      countryCode = None,
      lookupId = None
    )

    val address2 = address1.copy(addressLine1 = "2 Romford Road")
    val address3 = address1.copy(addressLine1 = "3 Romford Road")
    val address4 = address1.copy(addressLine1 = "2 Romford Road")
    val address5 = address1.copy(addressLine1 = "5 Romford Road")

    Map("1" -> address1, "2" -> address2, "3" -> address3, "4" -> address4, "5" -> address5)
  }

  val seedCacheWithUKAddressLookup: UserAnswers = emptyUserAnswers
    .set[UkAddressLookup](
      UkAddressLookupPage(1),
      UkAddressLookup(
        Some("1 Romford Road"),
        "TF1 4ER",
        testAddressData
      ))
    .success
    .value

  val seedCacheWithSingleUKAddress: UserAnswers = emptyUserAnswers
    .set[UkAddressLookup](
      UkAddressLookupPage(1),
      UkAddressLookup(
        Some("1 Romford Road"),
        "TF1 4ER",
        Map("1" -> Address(
          addressLine1 = "1 Romford Road",
          addressLine2 = Some("Wellington"),
          addressLine3 = Some("Telford"),
          addressLine4 = None,
          postcode = "TF1 4ER",
          countryCode = None,
          lookupId = None
        ))
      ))
    .success
    .value

  val seedCacheWithEmptyUKAddressList: UserAnswers = emptyUserAnswers
    .set[UkAddressLookup](
      UkAddressLookupPage(1),
      UkAddressLookup(
        Some("1 Romford Road"),
        "TF1 4ER",
        Map.empty
      ))
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" should {
      "render the choose address page" in {
        given.commonPrecondition
        addUserAnswersToSession(seedCacheWithUKAddressLookup)

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
          addUserAnswersToSession(seedCacheWithUKAddressLookup)

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
          addUserAnswersToSession(seedCacheWithUKAddressLookup)

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
          addUserAnswersToSession(seedCacheWithEmptyUKAddressList)

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
          addUserAnswersToSession(seedCacheWithSingleUKAddress)

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
