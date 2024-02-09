package uk.gov.hmrc.fhregistrationfrontend.controllers.businessPartners

import models.{CheckMode, Mode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.libs.ws.DefaultWSCookie
import play.mvc.Http.HeaderNames
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.BusinessPartnersEnterAddress
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.EnterAddressPage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersPartnershipEnterAddressControllerISpec
  extends Specifications with TestConfiguration {

  def route(mode: Mode): String = routes.BusinessPartnersPartnershipEnterAddressController.load(1, mode).url.drop(6)

  val address = BusinessPartnersEnterAddress(
    addressLine1 = "23 High Street",
    addressLine2 = Some("Park View"),
    addressLine3 = "Gloucester",
    postcode = Some("NE98 1ZZ")
  )

  val userAnswersWithPageData = emptyUserAnswers
    .set[BusinessPartnersEnterAddress](EnterAddressPage(1), address)
    .success
    .value

  val checkYouAnswersPage: String = routes.BusinessPartnersCheckYourAnswersController.load().url

  List(NormalMode, CheckMode).foreach { mode =>

    s"GET ${route(mode)}" when {

      "Render the Business Partners Enter the Partnership's Registered Office Address page" when {
        "there are userAnswers but no form data" in {
          given.commonPrecondition
          addUserAnswersToSession(emptyUserAnswers)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include("Enter the partnership’s registered office address?")
            page.getElementById("page-heading").text() must include("Enter")
            page.getElementById("page-heading").text() must include("registered office address")
            page.getElementById("enterAddress.line1").hasAttr("value") mustBe false
            page.getElementById("enterAddress.line2").hasAttr("value") mustBe false
            page.getElementById("enterAddress.line3").hasAttr("value") mustBe false
            page.getElementById("enterAddress.postcode").hasAttr("value") mustBe false
          }
        }

        "there are userAnswers with page data" in {
          given.commonPrecondition
          addUserAnswersToSession(userAnswersWithPageData)

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .get()

          whenReady(result) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title() must include("Enter the partnership’s registered office address?")
            page.getElementById("page-heading").text() must include("Enter")
            page.getElementById("page-heading").text() must include("registered office address")
            page.getElementById("enterAddress.line1").attr("value") must include("23 High Street")
            page.getElementById("enterAddress.line2").attr("value") must include("Park View")
            page.getElementById("enterAddress.line3").attr("value") must include("Gloucester")
            page.getElementById("enterAddress.postcode").attr("value") must include("NE98 1ZZ")
          }
        }
      }

      "There are no user answers in the database" should {
        "redirect to the start" in {
          given.commonPrecondition

          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(startCall.url)
          }
        }
      }
    }

    s"POST ${route(mode)}" when {
      Map("override" -> userAnswersWithPageData, "add" -> emptyUserAnswers). foreach { case (userAnswersAction, userAnswers) =>
        s"redirect to the Check Your Answers page and $userAnswersAction userAnswers" when {
          "only mandatory fields are populated" in {
            given.commonPrecondition
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "enterAddress.line1" -> Seq("1 Street"),
                "enterAddress.line2" -> Seq.empty,
                "enterAddress.line3" -> Seq("City name"),
                "enterAddress.postcode" -> Seq.empty
              ))

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(checkYouAnswersPage)
              val userAnswers = getUserAnswersFromSession.get
              val pageData = userAnswers.get(EnterAddressPage(1))
              pageData mustBe Some(BusinessPartnersEnterAddress(
                addressLine1 = "1 Street",
                addressLine2 = None,
                addressLine3 = "City name",
                postcode = None
              ))
            }
          }

          "all fields are populated" in {
            given.commonPrecondition
            addUserAnswersToSession(userAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "enterAddress.line1" -> Seq("23 High Street"),
                "enterAddress.line2" -> Seq("Park View"),
                "enterAddress.line3" -> Seq("Gloucester"),
                "enterAddress.postcode" -> Seq("NE98 1ZZ")
              ))

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(checkYouAnswersPage)
              val userAnswers = getUserAnswersFromSession.get
              val pageData = userAnswers.get(EnterAddressPage(1))
              pageData mustBe Some(address)
            }
          }
        }
      }

        "return a 400 error" when {
          "mandatory fields are not populated" in {
            given.commonPrecondition
            addUserAnswersToSession(emptyUserAnswers)

            val result = buildRequest(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map(
                "enterAddress.line1" -> Seq.empty,
                "enterAddress.line2" -> Seq.empty,
                "enterAddress.line3" -> Seq.empty,
                "enterAddress.postcode" -> Seq.empty
              ))

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
                "You must enter line 1 of the address")
              page.getElementsByClass("govuk-list govuk-error-summary__list").text() must include(
                "You must enter the Town or City of the address")
            }
          }
        }
      }
    }
}