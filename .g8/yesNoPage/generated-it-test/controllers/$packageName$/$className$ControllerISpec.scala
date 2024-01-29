package controllers.$packageName$

import controllers.ControllerITTestHelper
import models.SelectChange.$packageName;format="cap"$
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.$packageName$.$className$Page
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.WsTestClient

class $className$ControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/$url$"
  val checkRoutePath = "/change-$url$"

  "GET " + normalRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the $className$ page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, $packageName$BaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersFor$packageName;format="cap"$$className$Page.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, $packageName$BaseUrl + normalRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }
    testUnauthorisedUser($packageName$BaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + normalRoutePath)
  }

  s"GET " + checkRoutePath - {
    "when the userAnswers contains no data" - {
      "should return OK and render the $className$ page with no data populated" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, $packageName$BaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
            val radioInputs = page.getElementsByClass("govuk-radios__input")
            radioInputs.size() mustBe 2
            radioInputs.get(0).attr("value") mustBe "true"
            radioInputs.get(0).hasAttr("checked") mustBe false
            radioInputs.get(1).attr("value") mustBe "false"
            radioInputs.get(1).hasAttr("checked") mustBe false
          }
        }
      }
    }

    userAnswersFor$packageName;format="cap"$$className$Page.foreach { case (key, userAnswers) =>
      s"when the userAnswers contains data for the page with " + key + " selected" - {
        s"should return OK and render the page with " + key + " radio checked" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)

          WsTestClient.withClient { client =>
            val result1 = createClientRequestGet(client, $packageName$BaseUrl + checkRoutePath)

            whenReady(result1) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
              val radioInputs = page.getElementsByClass("govuk-radios__input")
              radioInputs.size() mustBe 2
              radioInputs.get(0).attr("value") mustBe "true"
              radioInputs.get(0).hasAttr("checked") mustBe key == "yes"
              radioInputs.get(1).attr("value") mustBe "false"
              radioInputs.get(1).hasAttr("checked") mustBe key == "no"
            }
          }
        }
      }
    }

    testUnauthorisedUser($packageName$BaseUrl + checkRoutePath)
    testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + checkRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + checkRoutePath)
  }

  s"POST " + normalRoutePath - {
    userAnswersFor$packageName;format="cap"$$className$Page.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the index controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, $packageName$BaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some($nextPage$.url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get($className$Page))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, $packageName$BaseUrl + normalRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some($nextPage$.url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get($className$Page))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, $packageName$BaseUrl + normalRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser($packageName$BaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    userAnswersFor$packageName;format="cap"$$className$Page.foreach { case (key, userAnswers) =>
      "when the user selects " + key - {
        "should update the session with the new value and redirect to the checkAnswers controller" - {
          "when the session contains no data for page" in {
            given
              .commonPrecondition

            setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, $packageName$BaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.$packageName;format="cap"$CYAController.onPageLoad.url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get($className$Page))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }

          "when the session already contains data for page" in {
            given
              .commonPrecondition

            setAnswers(userAnswers)
            WsTestClient.withClient { client =>
              val yesSelected = key == "yes"
              val result = createClientRequestPOST(
                client, $packageName$BaseUrl + checkRoutePath, Json.obj("value" -> yesSelected.toString)
              )

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(routes.$packageName;format="cap"$CYAController.onPageLoad.url)
                val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[Boolean]](None)(_.get($className$Page))
                dataStoredForPage.nonEmpty mustBe true
                dataStoredForPage.get mustBe yesSelected
              }
            }
          }
        }
      }
    }

    "when the user does not select yes or no" - {
      "should return 400 with required error" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, $packageName$BaseUrl + checkRoutePath, Json.obj("value" -> "")
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
            val errorSummary = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first()
            errorSummary
              .select("a")
              .attr("href") mustBe "#value"
            errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$" + ".error.required")
          }
        }
      }
    }
    testUnauthorisedUser($packageName$BaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
  }
}
