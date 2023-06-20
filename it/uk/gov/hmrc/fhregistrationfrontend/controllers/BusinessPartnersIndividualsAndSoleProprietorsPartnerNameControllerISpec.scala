package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.jsoup.Jsoup
import play.api.libs.ws.DefaultWSCookie
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import  uk.gov.hmrc.fhregistrationfrontend.forms.definitions.PartnerNameForm.{firstNameKey, lastNameKey}

class BusinessPartnersIndividualsAndSoleProprietorsPartnerNameControllerISpec
  extends Specifications with TestConfiguration {


  "GET /partner-name" when {

    "render the business partner IndividualsAndSoleProprietors partner name page" when {
      "the user is authenticated" in {
        given.commonPrecondition

        val result = buildRequest("/partner-name")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

        whenReady(result) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include("What is the name of the partner? - Business partners - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK")
        }
      }
    }
  }

  "POST /partner-name" when {
    "return 200" when {
      "the form is filled out correctly" in {
        given.commonPrecondition

        val result = buildRequest("/partner-name")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(firstNameKey -> Seq("Coca"), lastNameKey -> Seq("Cola")))

        whenReady(result) { res =>
          res.status mustBe 200
          res.body mustBe "Form submitted, with result: PartnerName(Coca,Cola)"
        }
      }
    }

    "return 400" when {
      "the form fields are left blank" in {
        given.commonPrecondition

        val result = buildRequest("/partner-name")
          .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
          .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
          .post(Map(firstNameKey -> Seq(""), lastNameKey -> Seq("")))

        whenReady(result) { res =>
          res.status mustBe 400
          val page = Jsoup.parse(res.body)
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the person’s first name")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include("Enter the person's last name")

        }
      }

      "fields contain invalid characters" in {
        given.commonPrecondition

        val result = buildRequest("/partner-name")
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
    }
  }

}