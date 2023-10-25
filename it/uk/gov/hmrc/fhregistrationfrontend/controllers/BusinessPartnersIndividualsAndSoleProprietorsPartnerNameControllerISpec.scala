package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.PartnerNameForm.{firstNameKey, lastNameKey}
import play.api.test.WsTestClient

class
BusinessPartnersIndividualsAndSoleProprietorsPartnerNameControllerISpec
  extends Specifications with TestConfiguration {

  val route = routes.BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController.load().url.drop(6)

  s"GET $route" when {

    "render the business partner IndividualsAndSoleProprietors partner name page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        val result = buildRequest(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include("What is the name of the partner? - Business partners - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK")
          }
      }
    }
  }

  s"POST $route" when {
    "return 200" when {
      "business type is neither Individual or Sole Proprietor and the form is filled out correctly" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "partnership")
          )
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(firstNameKey -> Seq("Coca"), lastNameKey -> Seq("Cola")))

        whenReady(result) { res =>
          res.status mustBe 200
          res.body mustBe "Form submitted, with result: PartnerName(Coca,Cola)"
        }
      }
    }

    "redirect to Business Partner NINO page" when {
      "business type is Individual and the form is filled out correctly" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "individual")
          )
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(firstNameKey -> Seq("Coca"), lastNameKey -> Seq("Cola")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnerNinoController.load().url)
        }
      }
    }

    "redirect to Business Partner Trading Name page" when {
      "business type is Sole Proprietor and the form is filled out correctly" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(
            DefaultWSCookie("mdtp", authAndSessionCookie),
            DefaultWSCookie("businessType", "sole-proprietor")
          )
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(firstNameKey -> Seq("Coca"), lastNameKey -> Seq("Cola")))

        whenReady(result) { res =>
          res.status mustBe 303
          res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnerTradingNameController.load().url)
        }
      }
    }

    "return 400" when {
      "the form fields are left blank" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(firstNameKey -> Seq(""), lastNameKey -> Seq("")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the person's first name")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the person's last name")
        }
      }

      "fields contain invalid characters" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(firstNameKey -> Seq("&&"), lastNameKey -> Seq("%%")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter only valid characters for a first name")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter only valid characters for a last name")
        }
      }

      "the first name field is left blank" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(firstNameKey -> Seq(""), lastNameKey -> Seq("Cola")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the person's first name")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() mustNot include("Enter the person's last name")
        }
      }

      "the last name field is left blank" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(firstNameKey -> Seq("Coca"), lastNameKey -> Seq("")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() mustNot include("Enter the person's first name")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the person's last name")
        }
      }

      "the first name field contains invalid characters" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(firstNameKey -> Seq("$$$"), lastNameKey -> Seq("cola")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter only valid characters for a first name")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() mustNot include("Enter only valid characters for a last name")
        }
      }

      "the last name field contains invalid characters" in {
        given.commonPrecondition

        val result = buildRequest(route)
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(firstNameKey -> Seq("Coca"), lastNameKey -> Seq("$$%%")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() mustNot include("Enter only valid characters for a first name")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter only valid characters for a last name")
        }
      }
    }
  }

}