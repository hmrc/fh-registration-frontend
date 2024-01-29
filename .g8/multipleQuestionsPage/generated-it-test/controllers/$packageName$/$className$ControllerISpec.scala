package controllers.$packageName$

import controllers.ControllerITTestHelper
import models.SelectChange.$packageName;format="cap"$
import models.$packageName$.$className$
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import pages.$packageName$.$className$Page
import play.api.http.HeaderNames
import play.api.i18n.Messages
import play.api.libs.json.{JsObject, Json}
import play.api.test.WsTestClient

class $className$ControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/$url$"
  val checkRoutePath = "/change-$url$"

  val $className;format="decap"$JsObject = Json.toJson($packageName$$className$).as[JsObject].value
  val $className;format="decap"$Map: collection.Map[String, String] = {
    $className;format="decap"$JsObject.map { case (fName, fValue) => fName -> fValue.as[String] }
  }

  val userAnswers = emptyUserAnswersFor$packageName;format="cap"$.set($className$Page, $packageName$$className$).success.value

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
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 2
            $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, _), index) =>
              inputFields.get(index).text() mustBe Messages("$packageName$.$className;format="decap"$." + fieldName)
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe false
            }
          }
        }
      }
    }

    s"when the userAnswers contains data for the page" - {
      s"should return OK and render the page with fields populated" in {
        given
          .commonPrecondition

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, $packageName$BaseUrl + normalRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 2
            $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>
              inputFields.get(index).text() mustBe Messages("$packageName$.$className;format="decap"$." + fieldName)
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe true
              inputFields.get(index).getElementById(fieldName).attr("value") mustBe fieldValue
            }
          }
        }
      }
    }
    testUnauthorisedUser($packageName$BaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + normalRoutePath)
  }

  "GET " + checkRoutePath - {
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
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 2
            $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, _), index) =>
              inputFields.get(index).text() mustBe Messages("$packageName$.$className;format="decap"$." + fieldName)
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe false
            }
          }
        }
      }
    }

    s"when the userAnswers contains data for the page" - {
      s"should return OK and render the page with fields populated" in {
        given
          .commonPrecondition

        setAnswers(userAnswers)

        WsTestClient.withClient { client =>
          val result1 = createClientRequestGet(client, $packageName$BaseUrl + checkRoutePath)

          whenReady(result1) { res =>
            res.status mustBe 200
            val page = Jsoup.parse(res.body)
            page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
            val inputFields = page.getElementsByClass("govuk-form-group")
            inputFields.size() mustBe 2
            $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>
              inputFields.get(index).text() mustBe Messages("$packageName$.$className;format="decap"$." + fieldName)
              inputFields.get(index).getElementById(fieldName).hasAttr("value") mustBe true
              inputFields.get(index).getElementById(fieldName).attr("value") mustBe fieldValue
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
    "when the user populates answers all questions" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + normalRoutePath, Json.toJson($packageName$$className$Diff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some($nextPage$.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[$className$]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $packageName$$className$Diff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + normalRoutePath, Json.toJson($packageName$$className$Diff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some($nextPage$.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[$className$]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $packageName$$className$Diff
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when no questions are answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, $packageName$BaseUrl + normalRoutePath, Json.toJson($className$("", ""))
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe $className;format="decap"$Map.size
            $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, _), index) =>
              val errorSummary = errorSummaryList.get(index)
              errorSummary
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$.error." + fieldName + ".required")
            }
          }
        }
      }
      $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, _), index) =>
        "when no answer is given for field" + fieldName in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          val invalidJson = $className;format="decap"$Map.foldLeft(Json.obj()) { case (current, (fn, fv)) =>
            val fieldValue = if (fn == fieldName) {
              ""
            } else {
              fn
            }
            current ++ Json.obj(fn -> fieldValue)
          }
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + normalRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummaryList
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummaryList.text() mustBe Messages("$packageName$.$className;format="decap"$.error." + fieldName + ".required")
            }
          }
        }
      }
    }

    testUnauthorisedUser($packageName$BaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + normalRoutePath, Some(Json.obj("value" -> "true")))
  }

  s"POST " + checkRoutePath - {
    "when the user populates answers all questions" - {
      "should update the session with the new values and redirect to the index controller" - {
        "when the session contains no data for page" in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + checkRoutePath, Json.toJson($packageName$$className$Diff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.$packageName;format="cap"$CYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[$className$]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $packageName$$className$Diff
            }
          }
        }

        "when the session already contains data for page" in {
          given
            .commonPrecondition

          setAnswers(userAnswers)
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + checkRoutePath, Json.toJson($packageName$$className$Diff)
            )

            whenReady(result) { res =>
              res.status mustBe 303
              res.header(HeaderNames.LOCATION) mustBe Some(routes.$packageName;format="cap"$CYAController.onPageLoad.url)
              val dataStoredForPage = getAnswers(userAnswers.id).fold[Option[$className$]](None)(_.get($className$Page))
              dataStoredForPage.nonEmpty mustBe true
              dataStoredForPage.get mustBe $packageName$$className$Diff
            }
          }
        }
      }
    }

    "should return 400 with required error" - {
      "when no questions are answered" in {
        given
          .commonPrecondition

        setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
        WsTestClient.withClient { client =>
          val result = createClientRequestPOST(
            client, $packageName$BaseUrl + checkRoutePath, Json.toJson($className$("", ""))
          )

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
            val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
              .first().getElementsByTag("li")
            errorSummaryList.size() mustBe $className;format="decap"$Map.size
            $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, _), index) =>
              val errorSummary = errorSummaryList.get(index)
              errorSummary
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$.error." + fieldName + ".required")
            }
          }
        }
      }
      $className;format="decap"$Map.zipWithIndex.foreach { case ((fieldName, _), index) =>
        "when no answer is given for field" + fieldName in {
          given
            .commonPrecondition

          setAnswers(emptyUserAnswersFor$packageName;format="cap"$)
          val invalidJson = $className;format="decap"$Map.foldLeft(Json.obj()) { case (current, (fn, fv)) =>
            val fieldValue = if (fn == fieldName) {
              ""
            } else {
              fn
            }
            current ++ Json.obj(fn -> fieldValue)
          }
          WsTestClient.withClient { client =>
            val result = createClientRequestPOST(
              client, $packageName$BaseUrl + checkRoutePath, invalidJson
            )

            whenReady(result) { res =>
              res.status mustBe 400
              val page = Jsoup.parse(res.body)
              page.title must include("Error: " + Messages("$packageName$.$className;format="decap"$" + ".title"))
              val errorSummaryList = page.getElementsByClass("govuk-list govuk-error-summary__list")
                .first()
              errorSummaryList
                .select("a")
                .attr("href") mustBe "#" + fieldName
              errorSummaryList.text() mustBe Messages("$packageName$.$className;format="decap"$.error." + fieldName + ".required")
            }
          }
        }
      }
    }

    testUnauthorisedUser($packageName$BaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))
    testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))  }
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + checkRoutePath, Some(Json.obj("value" -> "true")))  }
}
