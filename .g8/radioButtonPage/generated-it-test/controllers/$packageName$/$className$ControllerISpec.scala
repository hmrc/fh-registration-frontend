package uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$.$className$Page
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


  val $className;
  format = "decap" $ = $className$("test1", "test2")

  val userAnswersWithPageData = emptyUserAnswers
    .set[$className$]($className$Page(1), $className;
  format = "decap" $
  )
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
              radioInputs.size() mustBe $className$.values.size

              $className$.values.zipWithIndex.foreach { case (radio1, index1) =>
                radioInputs.get(index1).attr("value") mustBe radio1.toString
                radioInputs.get(index1).hasAttr("checked") mustBe false
              }
            }
          }
        }
      }

      $className$.values.zipWithIndex.foreach { case (radio, index) =>
        s"when the userAnswers contains data for the page with " + radio.toString + " selected" should {
          s"return OK and render the page with " + radio.toString + " radio checked" in {
            given
              .commonPrecondition

            addUserAnswersToSession(userAnswersWithPageData)

            WsTestClient.withClient { client =>
              val result1 = buildRequestFromRoute(route(mode))
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

              whenReady(result1) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title must include("$title$")
                val radioInputs = page.getElementsByClass("govuk-radios__input")
                radioInputs.size() mustBe $className$.values.size

                $className$.values.zipWithIndex.foreach { case (radio1, index1) =>
                  radioInputs.get(index1).attr("value") mustBe radio1.toString
                  radioInputs.get(index1).hasAttr("checked") mustBe index == index1
                }
              }
            }
          }
        }
      }

      "there are no user answers in the database" should {
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
      "the user populates answers all questions" should {
        val expectedUrl = if (mode == CheckMode) {
          $packageName$.routes.$packageName;
          format = "cap" $CYAController
        .load(1).url
        } else {
          $nextPage$.url.replace("/fhdds", "")
        }
        s"update the user answers with the new values and redirect to" + expectedUrl when {
          "the user answers contain no page data" in {
            given.commonPrecondition

            addUserAnswersToSession(emptyUserAnswers)
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(
                DefaultWSCookie("mdtp", authAndSessionCookie)
              )
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map("value" -> radio.toString))

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).get must include(expectedUrl)
              val userAnswers = getUserAnswersFromSession.get
              val pageData = userAnswers.get($className$Page(1))
              pageData mustBe Some(radio)
            }
          }
        }

        s"override the user answers with the new values and redirect to" + expectedUrl when {
          "the user answers contains page data" in {
            given.commonPrecondition

            addUserAnswersToSession(userAnswersWithPageData)
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(
                DefaultWSCookie("mdtp", authAndSessionCookie)
              )
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map("value" -> radio.toString))

            whenReady(result1) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION).get must include(expectedUrl)
              val userAnswers = getUserAnswersFromSession.get
              val pageData = userAnswers.get($className$Page(1))
              pageData mustBe Some(radio)
            }
          }
        }
      }
      "should return 400 with required error" when {
        "no questions are answered" in {
          given.commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)
          val result1 = buildRequestFromRoute(route(mode))
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie)
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("value" -> ""))


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
              .attr("href") mustBe "#value_0"
            errorSummary.text() mustBe "Select an option"
          }
        }
      }
    }
  }
}
