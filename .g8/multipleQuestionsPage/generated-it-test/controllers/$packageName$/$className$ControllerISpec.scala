package uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$.$className$Page
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.controllers._

class $className$ControllerISpec extends Specifications with TestConfiguration {

  def route(mode: Mode): Call = $packageName$.routes.$className$Controller.onPageLoad(1, mode)


  val $className;format="decap"$ = $className$("test1", "test2")

  val userAnswersWithPageData = emptyUserAnswers
    .set[$className$]($className$Page(1), $className;format="decap"$)
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"GET ${route(mode)}" when {
      "the userAnswers contains no data" - {
        "should return OK and render the $className$ page with no data populated" in {
          given
            .commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)

          WsTestClient.withClient { client =>
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include($title$)
              val inputFields = page.getElementsByClass("govuk-form-group")
              inputFields.size() mustBe 2
              inputFields.get(0).text() mustBe $className;format="decap"$.$field1Name$
              inputFields.get(0).getElementById($field1Name$).hasAttr("value") mustBe false
              inputFields.get(1).text() mustBe $className;format = "decap"$.$field2Name$
              inputFields.get(1).getElementById($field2Name$).hasAttr("value") mustBe false
            }
          }
        }
      }

      "the userAnswers contains no data" - {
        "should return OK and render the $className$ page with no data populated" in {
          given
            .commonPrecondition

          addUserAnswersToSession(userAnswersWithPageData)

          WsTestClient.withClient { client =>
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include($title$)
              val inputFields = page.getElementsByClass("govuk-form-group")
              inputFields.size() mustBe 2
              inputFields.get(0).text() mustBe $className;format ="decap"$.$field1Name$
              inputFields.get(0).getElementById("$field1Name$").hasAttr("value") mustBe true
              inputFields.get(0).getElementById("$field1Name$").attr("value") mustBe "test1"
              inputFields.get(1).text() mustBe $className;format="decap"$.$field2Name$
              inputFields.get(1).getElementById("$field2Name$").hasAttr("value") mustBe true
              inputFields.get(1).getElementById("$field2Name$").attr("value") mustBe "test2"
            }
          }
        }
      }

      "there is no user answers in the database" should {
        "redirect to the start" in {
          given.commonPrecondition
          val result = buildRequest(route(mode))
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 303
            res.header(HeaderNames.LOCATION) mustBe Some(routes.Application.main().url)
          }
        }
      }
    }

    s"POST ${route(mode)}" when {
      "the user populates answers all questions" should {
        val expectedUrl = if (mode == CheckMode) {
          $packageName;format="cap"$CYAController.load(index)
        } else {
          $nextPage$
        }
        s"update the user answers with the new values and redirect to $expectedUrl" when {
          "the user answers contain no page data" in {
            given.commonPrecondition

            addUserAnswersToSession(emptyUserAnswers)
            WsTestClient.withClient { client =>
              val result1 = buildRequestFromRoute(route(mode))
                .addCookies(
                  DefaultWSCookie("mdtp", authAndSessionCookie)
                )
                .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                .post(Map("$field1Name$" -> Seq("Coca"), "$field2Name$" -> Seq("Cola")))

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.Application.main().url)
                val userAnswers = getUserAnswersFromSession.get
                val pageData = userAnswers.get($className$Page(1))
                pageData mustBe Some($className$("Coca", "Cola"))
              }
            }
          }
        }

        s"override the user answers with the new values and redirect to $expectedUrl" when {
          "the user answers contains page data" in {
            given.commonPrecondition

            addUserAnswersToSession(userAnswersWithPageData)
            WsTestClient.withClient { client =>
              val result1 = buildRequestFromRoute(route(mode))
                .addCookies(
                  DefaultWSCookie("mdtp", authAndSessionCookie)
                )
                .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                .post(Map("$field1Name$" -> Seq("Coca"), "$field2Name$" -> Seq("Cola")))

              whenReady(result1) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.Application.main().url)
                val userAnswers = getUserAnswersFromSession.get
                val pageData = userAnswers.get($className$Page(1))
                pageData mustBe Some($className$("Coca", "Cola"))
              }
            }
          }
        }
      }

      "should return 400 with required error" - {
        "when no questions are answered" in {
          given.commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(
                DefaultWSCookie("mdtp", authAndSessionCookie)
              )
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map("$field1Name$" -> Seq(""), "$field2Name$" -> Seq("")))


            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: $title$")
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first().getElementsByTag("li")
              errorSummaryList.size() mustBe 2
              val errorSummary1 = errorSummaryList.get(0)
              errorSummary1
                .select("a")
                .attr("href") mustBe "#$field1Name$"
              errorSummary.text() mustBe Messages("$packageName$.$className;format="
              decap"$.error." + "$field1Name$" + ".required"
              )
              val errorSummary2 = errorSummaryList.get(1)
              errorSummary
                .select("a")
                .attr("href") mustBe "#$field2Name$"
              errorSummary.text() mustBe Messages("$packageName$.$className;format="
              decap"$.error." + "$field2Name$" + ".required"
              )
            }
          }
        }
        "when no answer is given for $field1Name$" in {
          given.commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(
                DefaultWSCookie("mdtp", authAndSessionCookie)
              )
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map("$field1Name$" -> Seq(""), "$field2Name$" -> Seq("cola")))


            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: $title$")
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first().getElementsByTag("li")
              errorSummaryList.size() mustBe 1
              val errorSummary1 = errorSummaryList.get(0)
              errorSummary1
                .select("a")
                .attr("href") mustBe "#$field1Name$"
              errorSummary.text() mustBe Messages("$packageName$.$className;format="
              decap"$.error." + "$field1Name$" + ".required"
              )
            }
          }
        }
        "when no answer is given for $field1Name$" in {
          given.commonPrecondition

          addUserAnswersToSession(emptyUserAnswers)
          WsTestClient.withClient { client =>
            val result1 = buildRequestFromRoute(route(mode))
              .addCookies(
                DefaultWSCookie("mdtp", authAndSessionCookie)
              )
              .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
              .post(Map("$field1Name$" -> Seq(""), "$field2Name$" -> Seq("cola")))


            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: $title$")
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first().getElementsByTag("li")
              errorSummaryList.size() mustBe 1
              val errorSummary1 = errorSummaryList.get(0)
              errorSummary1
                .select("a")
                .attr("href") mustBe "#$field2Name$"
              errorSummary.text() mustBe Messages("$packageName$.$className;format="
              decap"$.error." + "$field2Name$" + ".required"
              )
            }
          }
        }
      }
    }
  }
}
