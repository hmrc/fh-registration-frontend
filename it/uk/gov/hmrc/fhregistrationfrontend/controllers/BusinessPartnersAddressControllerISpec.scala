package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, UkAddressLookup}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.UkAddressLookupPage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import models.NormalMode
import scala.collection.immutable.Seq
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.mvc.Http.HeaderNames

class BusinessPartnersAddressControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersAddressController.load(1, mode).url.drop(6)

  val index = 1
  val userAnswersWithPageData: UserAnswers = emptyUserAnswers
    .set[UkAddressLookup](UkAddressLookupPage(1), UkAddressLookup(Some("44 test lane"), "SW1A 2AA", Map(
      "1" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, None),
      "2" -> Address("77 test lane", None, None, None, "SW1A 2AA", None, None))))
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>

    s"GET ${route(mode)}" when {
      "render the business partner address page with answers NOT prepopulated" when {
          "the user is authenticated" in {
            given.commonPrecondition
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title() must include("What is the partner’s address?")
              page.getElementsByTag("h1").text() must include("What is Test User’s address?")
              val postcodeField = page.getElementById("partnerPostcode")
              postcodeField.hasAttr("value") mustBe false
              val addressLineField = page.getElementById("partnerAddressLine")
              addressLineField.hasAttr("value") mustBe false
            }
          }
      }

      "render the business partner address page with answers prepopulated" when {
        "the user is authenticated" in {
          given.commonPrecondition
          addUserAnswersToSession(userAnswersWithPageData)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include("What is the partner’s address?")
            page.getElementsByTag("h1").text() must include("What is Test User’s address?")
            val postcodeField = page.getElementById("partnerPostcode")
            postcodeField.`val` mustBe "SW1A 2AA"
            val addressLineField = page.getElementById("partnerAddressLine")
            addressLineField.`val` mustBe "44 test lane"
          }
        }
      }
    }

    s"POST ${route(mode)}" when {
      "the postcode is valid, address line provided and multiple addresses are returned from address lookup" should {
        "redirect to choose address" in {
          given.commonPreconditionWithMultipleAddressLookup(true)
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq("1"),
              "partnerPostcode" -> Seq("AB1 2YZ")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersChooseAddressController.load(index, mode).url)
          }
        }
      }

      "the postcode is valid, address line not provided and addresses are returned from address lookup" should {
        "redirect to choose address" in {
          given.commonPreconditionWithMultipleAddressLookup(true)
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq.empty,
              "partnerPostcode" -> Seq("AB1 2YZ")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersChooseAddressController.load(index, mode).url)
          }
        }
      }

      "the postcode is valid and matches what is stored in UserAnswers" should {
        "redirect to choose address" in {
          given.commonPrecondition
          addUserAnswersToSession(userAnswersWithPageData)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq.empty,
              "partnerPostcode" -> Seq("SW1A 2AA")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersChooseAddressController.load(index, mode).url)
          }
        }
      }

      "the postcode is valid and a single address is returned from address lookup" should {
        "redirect to confirm address" in {
          given.commonPreconditionWithSingleAddressLookup(true)
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq("1"),
              "partnerPostcode" -> Seq("AB1 2YZ")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersConfirmAddressController.load(index, mode).url)
          }
        }
      }

      "the postcode is valid and no addresses are returned from address lookup" should {
        "redirect to cannot find address" in {
          given.commonPreconditionWithEmptyAddressLookup(true)
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq.empty,
              "partnerPostcode" -> Seq("AB1 2YZ")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersCannotFindAddressController.load(index, mode).url)
          }
        }
      }

      "return a 400 - BadRequest" when {
        "the postcode is valid, address line not provided and address lookup returns an error" in {
            given.commonPreconditionWithMultipleAddressLookup(false)
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
              .post(Map("partnerAddressLine" -> Seq.empty,
                "partnerPostcode" -> Seq("AB1 2YZ")))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-error-summary").text() must include("Sorry, there was problem performing this search, please try again and if the problem persists then enter the address manually")
            }
        }

        "the postcode is not populated" in {
            given.commonPreconditionWithMultipleAddressLookup(true)
            addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
              .post(Map("partnerAddressLine" -> Seq("1"),
                "partnerPostcode" -> Seq.empty))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-error-summary").text() must include("Enter the postcode of the address")
            }
          }
        }

        "postcode invalid format" in {
            given.commonPreconditionWithMultipleAddressLookup(true)
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
              .post(Map("partnerAddressLine" -> Seq("1"),
                "partnerPostcode" -> Seq("A")))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Enter a valid postcode")
            }
        }

        "address line contains invalid characters" in {
            given.commonPreconditionWithMultipleAddressLookup(true)
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
              .post(Map("partnerAddressLine" -> Seq("The lane;"),
                "partnerPostcode" -> Seq("AB1 2YZ")))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-error-summary").text() must include("only include letters a to z, numbers, apostrophes, commas, dashes, exclamation marks, forward slashes, full stops, hyphens, quotation marks, round brackets and spaces")
            }
        }

        "address line contains more than 35 characters" in {
            given.commonPreconditionWithMultipleAddressLookup(true)
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
              .post(Map("partnerAddressLine" -> Seq("this Address Line is too long this Address Line is too long"),
                "partnerPostcode" -> Seq("AB1 2YZ")))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-error-summary").text() must include("Address lines must not be longer than 35 characters")
            }
        }
     }
  }
}