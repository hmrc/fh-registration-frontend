package uk.gov.hmrc.fhregistrationfrontend.controllers.businessPartners

import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, UkAddressLookup}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.UkAddressLookupPage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersPartnershipRegisteredAddressControllerISpec
  extends Specifications with TestConfiguration {

  lazy val index = 1
  def confirmPartnershipRegAddressUrl(mode: Mode): String = routes.BusinessPartnersPartnershipConfirmRegisteredAddressController.load(index, mode).url
  def chooseAddressUrl(mode: Mode): String = routes.BusinessPartnersChooseAddressController.load(1, mode).url
  def cannotFindAddressUrl(mode: Mode): String = routes.BusinessPartnersCannotFindAddressController.load(1, mode).url
  val userAnswersWithPageData: UserAnswers = emptyUserAnswers
    .set[UkAddressLookup](UkAddressLookupPage(1), UkAddressLookup(Some("44 test lane"), "SW1A 2AA", Map(
      "1" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, None),
      "2" -> Address("77 test lane", None, None, None, "SW1A 2AA", None, None))))
    .success
    .value

  def route(mode: Mode): String = routes.BusinessPartnersPartnershipRegisteredAddressController.load(index, mode).url.drop(6)

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" should {
      "render the business partner address page with answers prepopulated" in {
        given.commonPrecondition
        addUserAnswersToSession(userAnswersWithPageData)

        val result = buildRequest(route(mode))
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("What is the partnership’s registered office address?")
          page.getElementsByTag("h1").text() must include("What is Test User’s registered office address?")
          val postcodeField = page.getElementById("partnerPostcode")
          postcodeField.`val` mustBe "SW1A 2AA"
          val addressLineField = page.getElementById("partnerAddressLine")
          addressLineField.`val` mustBe "44 test lane"
        }
      }

      "render the business partner address page with no answers prepopulated" in {
        given.commonPrecondition
        addUserAnswersToSession(emptyUserAnswers)

        val result = buildRequest(route(mode))
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title() must include("What is the partnership’s registered office address?")
          page.getElementsByTag("h1").text() must include("What is Test User’s registered office address?")
          val postcodeField = page.getElementById("partnerPostcode")
          postcodeField.hasAttr("value") mustBe false
          val addressLineField = page.getElementById("partnerAddressLine")
          addressLineField.hasAttr("value") mustBe false
        }
      }
    }

    s"POST ${route(mode)}" should {
      "redirect to the choose address page" when {
        "the postcode and address line are valid and multiple address results are returned" in {
          given.commonPreconditionWithMultipleAddressLookup(true)
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map(
              "partnerAddressLine" -> Seq("1"),
              "partnerPostcode" -> Seq("TF1 4ER")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(chooseAddressUrl(mode))
          }
        }

        "the postcode is valid but no address line is provided and multiple address results are returned" in {
          given.commonPreconditionWithMultipleAddressLookup(true)
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map(
              "partnerAddressLine" -> Seq.empty,
              "partnerPostcode" -> Seq("TF1 4ER")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(chooseAddressUrl(mode))
          }
        }
      }

      "redirect to the Confirm Address page" when {
       "address entered where single address found" in {
         given.commonPreconditionWithSingleAddressLookup(true)
         addUserAnswersToSession(emptyUserAnswers)

         val result = buildRequest(route(mode))
           .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
           .withHttpHeaders(xSessionId,
             "Csrf-Token" -> "nocheck")
           .post(Map(
             "partnerAddressLine" -> Seq("1 test drive"),
             "partnerPostcode" -> Seq("AA1 2BB")
           ))

         whenReady(result) { res =>
           res.status mustBe 303
           res.header(HeaderNames.LOCATION) mustBe Some(confirmPartnershipRegAddressUrl(mode))
         }
       }
      }

      "redirect to the Cannot Find Address page" when {
        "address entered is not found" in {
          given.commonPreconditionWithEmptyAddressLookup(true)
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId,
              "Csrf-Token" -> "nocheck")
            .post(Map(
              "partnerAddressLine" -> Seq.empty,
              "partnerPostcode" -> Seq("HR33 7GP")
            ))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(cannotFindAddressUrl(mode))
          }
        }
      }

      "return 400 - BadRequest" when {
        "postcode not populated" in {
          given.commonPrecondition
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

        "postcode is in an invalid format" in {
          given.commonPrecondition
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
          given.commonPrecondition
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
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).withHttpHeaders(xSessionId,
            "Csrf-Token" -> "nocheck")
            .post(Map("partnerAddressLine" -> Seq("qwertyuiopasdfghjklzxcvbnmqwkydvkdsgvisudgfkjsdvkjsdcjkdh"),
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
}
