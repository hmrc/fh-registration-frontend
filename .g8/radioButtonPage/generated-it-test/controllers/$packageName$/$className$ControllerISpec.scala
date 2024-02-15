package uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$.$className$Page
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.controllers._
import models.{CheckMode, Mode, NormalMode}
import play.api.mvc.Call
import play.api.libs.ws.DefaultWSCookie
import org.scalatest.TryValues.convertTryToSuccessOrFailure

class $className$ControllerISpec extends Specifications with TestConfiguration {

  def route(mode: Mode): Call = $packageName$.routes.$className$Controller.onPageLoad(1, mode)

  val userAnswersWith$option1key$Selected = emptyUserAnswers
    .set[$className$.Value]($className$Page(1), $className$.$option1key$)
    .success
    .value

  val userAnswersWith$option2key$Selected = emptyUserAnswers
    .set[$className$.Value]($className$Page(1), $className$.$option2key$)
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET" + route(mode).url when {
      "the userAnswers contains no data" should {
        "return OK and render the $className$ page with no data populated" in {
          given
            .commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)

          WsTestClient.withClient { client =>
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include("$title$")
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "$option1key$"
              radioInputs.get(0).hasAttr("checked") mustBe false
              radioInputs.get(1).attr("value") mustBe "$option2key$"
              radioInputs.get(1).hasAttr("checked") mustBe false
            }
          }
        }
      }

      "the userAnswers contains data with $option1msg$ selected" should {
        "return OK and render the $className$ page with data populated" in {
          given
            .commonPrecondition

          addUserAnswersToSession(userAnswersWith$option1key$Selected)

          WsTestClient.withClient { client =>
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include("$title$")
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "$option1key$"
              radioInputs.get(0).hasAttr("checked") mustBe true
              radioInputs.get(1).attr("value") mustBe "$option2key$"
              radioInputs.get(1).hasAttr("checked") mustBe false
            }
          }
        }
      }

      "the userAnswers contains data with $option2key$ selected" should {
        "return OK and render the $className$ page with data populated" in {
          given
            .commonPrecondition

          addUserAnswersToSession(userAnswersWith$option2key$Selected)

          WsTestClient.withClient { client =>
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include("$title$")
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "$option1key$"
              radioInputs.get(0).hasAttr("checked") mustBe false
              radioInputs.get(1).attr("value") mustBe "$option2key$"
              radioInputs.get(1).hasAttr("checked") mustBe true
            }
          }
        }
      }

      "there is no user answers in the database" should {
        "redirect to the start" in {
          given.commonPrecondition
          val result = buildRequestFromRoute(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.Application.main().url)
          }
        }
      }
    }

    s"POST" + route(mode).url when {
      val expectedUrl = if (mode == CheckMode) {
        $packageName$.routes.$packageName;format="cap"$CYAController.load(1).url
      } else {
        $nextPage$.url.replace("/fhdds", "")
      }
      "the user selects $option1key$" should {
        s"update the user answers with the new values and redirect to" + expectedUrl when {
          "the user answers contain no page data" in {
            given.commonPrecondition

            addUserAnswersToSession(emptyUserAnswers)
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(
                DefaultWSCookie("mdtp", authAndSessionCookie)
              )
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map("value" -> Seq("$option1key$")))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).get must include(expectedUrl)
              val userAnswers = getUserAnswersFromSession.get
              val pageData = userAnswers.get($className$Page(1))
              pageData mustBe Some($className$.$option1key$)
            }
          }
        }

        s"override the user answers with the new values and redirect to" + expectedUrl when {
          "the user answers contains page data" in {
            given.commonPrecondition

            addUserAnswersToSession(userAnswersWith$option2key$Selected)
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(
                DefaultWSCookie("mdtp", authAndSessionCookie)
              )
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map("value" -> Seq("$option1key$")))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).get must include(expectedUrl)
              val userAnswers = getUserAnswersFromSession.get
              val pageData = userAnswers.get($className$Page(1))
              pageData mustBe Some($className$.$option1key$)
            }
          }
        }
      }

      "the user selects $option2key$" should {
        s"update the user answers with the new values and redirect to" + expectedUrl when {
          "the user answers contain no page data" in {
            given.commonPrecondition

            addUserAnswersToSession(emptyUserAnswers)
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(
                DefaultWSCookie("mdtp", authAndSessionCookie)
              )
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map("value" -> Seq("$option2key$")))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).get must include(expectedUrl)
              val userAnswers = getUserAnswersFromSession.get
              val pageData = userAnswers.get($className$Page(1))
              pageData mustBe Some($className$.$option2key$)
            }
          }
        }

        s"override the user answers with the new values and redirect to" + expectedUrl when {
          "the user answers contains page data" in {
            given.commonPrecondition

            addUserAnswersToSession(userAnswersWith$option1key$Selected)
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(
                DefaultWSCookie("mdtp", authAndSessionCookie)
              )
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map("value" -> Seq("$option2key$")))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).get must include(expectedUrl)
              val userAnswers = getUserAnswersFromSession.get
              val pageData = userAnswers.get($className$Page(1))
              pageData mustBe Some($className$.$option2key$)
            }
          }
        }
      }

      "should return 400 with required error" when {
        "the yes or no is not answered" in {
          given.commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)
          val result1 = buildRequestFromRoute(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie)
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("value" -> Seq("")))


          whenReady(result1) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: $title$")
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe 1
            val errorSummary = errorSummaryList.get(0)
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe "Select $className;format="decap"$"
          }
        }
      }
    }
  }
}
