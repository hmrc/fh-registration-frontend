package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.PartnerNameForm.{firstNameKey, lastNameKey}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.PartnerName
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.IndividualsAndSoleProprietorsPartnerNamePage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class
BusinessPartnersIndividualsAndSoleProprietorsPartnerNameControllerISpec
  extends Specifications with TestConfiguration {
  def route(mode: Mode) = routes.BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController.load(1, mode).url.drop(6)

  val partnerName = PartnerName("test", "user")
  val userAnswersWithPageData = emptyUserAnswers
    .set[PartnerName](IndividualsAndSoleProprietorsPartnerNamePage(1), partnerName)
    .success
    .value
  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" when {

      "when the user is authenticated" should {
        "render the business partner IndividualsAndSoleProprietors partner name page with answers not prepopulated" when {
          "there are useranswers but no form data" in {
            given.commonPrecondition

           addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include("What is the name of the partner? - Business partners - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK")
              val firstNameField = page.getElementById("firstName")
              firstNameField.hasAttr("value") mustBe false
              val lastNameField = page.getElementById("lastName")
              lastNameField.hasAttr("value") mustBe false
            }
          }
        }

        "render the business partner IndividualsAndSoleProprietors partner name page with answers prepopulated" when {
          "there is are userAnswers with page data" in {
            given.commonPrecondition
            addUserAnswersToSession(userAnswersWithPageData)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include("What is the name of the partner? - Business partners - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK")
              val firstNameField = page.getElementById("firstName")
              firstNameField.attr("value") mustBe "test"
              val lastNameField = page.getElementById("lastName")
              lastNameField.attr("value") mustBe "user"
            }
          }
        }
      }

      "there is no user answers in the database" should {
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
      "the business type is Individual and the form is filled out correctly" should {
        Map("override" -> userAnswersWithPageData, "add" -> emptyUserAnswers).foreach { case (uaAction, userAnswers) =>
          s"redirect to Business Partner NINO page and $uaAction userAnswers" when {
            "business type is Individual and the form is filled out correctly" in {
              given.commonPrecondition

              addUserAnswersToSession(userAnswers)

              val result = buildRequest(route(mode))
                .addCookies(
                  DefaultWSCookie("mdtp", authAndSessionCookie),
                  DefaultWSCookie("businessType", "individual")
                )
                .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                .post(Map(firstNameKey -> Seq("Coca"), lastNameKey -> Seq("Cola")))

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnerNinoController.load().url)
                val userAnswers = getUserAnswersFromSession.get
                val pageData = userAnswers.get(IndividualsAndSoleProprietorsPartnerNamePage(1))
                pageData mustBe Some(PartnerName("Coca", "Cola"))
              }
            }
          }

          s"redirect to Business Partner Trading Name page and $uaAction userAnswers" when {
            "business type is Sole Proprietor and the form is filled out correctly" in {
              given.commonPrecondition
              addUserAnswersToSession(emptyUserAnswers)

              val result = buildRequest(route(mode))
                .addCookies(
                  DefaultWSCookie("mdtp", authAndSessionCookie),
                  DefaultWSCookie("businessType", "sole-proprietor")
                )
                .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                .post(Map(firstNameKey -> Seq("Coca"), lastNameKey -> Seq("Cola")))

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersTradingNameController.load().url)
                val userAnswers = getUserAnswersFromSession.get
                val pageData = userAnswers.get(IndividualsAndSoleProprietorsPartnerNamePage(1))
                pageData mustBe Some(PartnerName("Coca", "Cola"))
              }
            }
          }
        }
      }

      "business type is neither Individual or Sole Proprietor" should {
        "redirect to start of business details" in {
          given.commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "partnership")
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(firstNameKey -> Seq("Coca"), lastNameKey -> Seq("Cola")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersController.load().url)
          }
        }
      }

      "return 400" when {
        "the form fields are left blank" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
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
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
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
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
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
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
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
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
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
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
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

      "there is no user answers in the database" should {
        "redirect to the start of BusinessPartners" in {
          given.commonPrecondition
          val result = buildRequest(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "partnership")
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map(firstNameKey -> Seq("Coca"), lastNameKey -> Seq("Cola")))

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.BusinessPartnersController.load().url)
          }
        }
      }
    }
  }

}