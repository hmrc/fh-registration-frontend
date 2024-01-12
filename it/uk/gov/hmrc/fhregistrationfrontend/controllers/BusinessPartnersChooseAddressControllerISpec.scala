package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.{AddressPage, UkAddressLookupPage}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, ChooseAddress, UkAddressLookup}
import org.scalatest.TryValues.convertTryToSuccessOrFailure

class BusinessPartnersChooseAddressControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersChooseAddressController.load(1, mode).url.drop(6)

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

    Map(
      "1" -> address1,
      "2" -> address2,
      "3" -> address3,
      "4" -> address4,
      "5" -> address5
    )
  }

  val cachedUkAddressLookup: UkAddressLookup = UkAddressLookup(
    Some("1 Romford Road"),
    "TF1 4ER",
    testAddressData
  )
  def createCachedukAddressLookup(answers: UkAddressLookup): UserAnswers =
    emptyUserAnswers
      .set[UkAddressLookup](UkAddressLookupPage(1), answers)
      .success
      .value

  val userAnswersWithPageData: UserAnswers = emptyUserAnswers
    .set[Address](AddressPage(1), testAddressData.head._2)
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>

    s"GET ${route(mode)}" should {
      "render the choose address page" in {
        given.commonPrecondition
        val userAnswers = createCachedukAddressLookup(cachedUkAddressLookup)
        addUserAnswersToSession(userAnswers)
        addUserAnswersToSession(emptyUserAnswers)

        val result = buildRequest(route(mode))
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("Choose address")
        }
      }

      "there are no user answers in the database" should {
        "redirect to the start of BusinessPartners" in {
          given.commonPrecondition

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersController.load().url)
          }
        }
      }
    }

    s"POST ${route(mode)}" when {
      "redirect the user to the Check Your Answers page" when {
        "the form has no errors" in {
          given.commonPrecondition
          val userAnswers = createCachedukAddressLookup(cachedUkAddressLookup)
          addUserAnswersToSession(userAnswers)
          addUserAnswersToSession(emptyUserAnswers)

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
      }
    }
  }
}
