package controllers.$packageName$

import controllers.ControllerITTestHelper
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.{convertToAnyMustWrapper, include}
import play.api.i18n.Messages
import play.api.test.WsTestClient
import models.SelectChange.$packageName;format="cap"$

class $className$ControllerISpec extends ControllerITTestHelper {

  val normalRoutePath = "/$url$"

  "GET " + normalRoutePath - {
    "should return OK and render the $className$ page" in {
      given
        .commonPrecondition

      setAnswers(emptyUserAnswersFor$packageName;format="cap"$)

      WsTestClient.withClient { client =>
        val result1 = createClientRequestGet(client, $packageName$BaseUrl + normalRoutePath)

        whenReady(result1) { res =>
          res.status mustBe 200
          val page = Jsoup.parse(res.body)
          page.title must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
        }
      }
    }
    testUnauthorisedUser($packageName$BaseUrl + normalRoutePath)
    testAuthenticatedUserButNoUserAnswers($packageName$BaseUrl + normalRoutePath)
    testAuthenticatedWithUserAnswersForUnsupportedJourneyType($packageName;format="cap"$, $packageName$BaseUrl + normalRoutePath)
  }
}
