package uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$

import com.codahale.metrics.SharedMetricRegistries
import models.{CheckMode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$.$className$Page
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.html.$packageName$.v2.$className$View
import uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$.$className$Form
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.$className$Controller
import uk.gov.hmrc.fhregistrationfrontend.controllers.ControllerSpecWithGuiceApp

import scala.concurrent.Future

class $className$ControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  val mockSessionCache: SessionRepository = mock[SessionRepository]
  val index = 1
  val $className;format="decap"$View: $className$View = app.injector.instanceOf[$className$View]
  val controller =
    new $className$Controller(commonDependencies, $className;format="decap"$View, mockActions, mockSessionCache, mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>
    "onPageLoad when in " + mode should {
      "Render the $className;format="decap"$ page" when {
        "there are userAnswers but no page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction$packageName;format="cap"$(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.onPageLoad(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("$title$")
          reset(mockActions)
        }

        s"there are userAnswers with page data for ${$className$.values.head}" in {
          val dataOption = $className$.values.head
          val userAnswers = UserAnswers(testUserId)
            .set($className$Page(index), dataOption)
            .success
            .value
          setupDataRequiredAction$packageName;format="cap"$(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.onPageLoad(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("$title$")
          reset(mockActions)
        }

        s"there are userAnswers with page data for ${$className$.values.last}" in {
          val dataOption = $className$.values.last
          val userAnswers = UserAnswers(testUserId)
            .set($className$Page(index), dataOption)
            .success
            .value
          setupDataRequiredAction$packageName;format="cap"$(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.onPageLoad(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("$title$")
          reset(mockActions)
        }
      }
    }

    s"onSubmit when in " + mode should {
      val expectedUrl = if (mode == CheckMode) {
        routes.$packageName;format="cap"$CYAController.load(index).url
      } else {
        $nextPage$.url.replace("/fhdds", "")
      }
      "save the answer to database and redirect to " + expectedUrl when {
        "the user answers doesn't contain page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction$packageName;format="cap"$(userAnswers, mode)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "value" -> $className$.values.head.toString
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.onSubmit(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(expectedUrl)
          reset(mockActions)
        }
      }
    }
  }
}
