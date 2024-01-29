package controllers.$packageName$

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.$packageName$.$className$View
import models.SelectChange.$packageName;format="cap"$
class $className$ControllerSpec extends SpecBase {

  lazy val $className;format="decap"$Route = routes.$className$Controller.onPageLoad().url


  "$className$ Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswersFor$packageName;format="cap"$)).build()

      running(application) {
        val request = FakeRequest(GET, $className;format="decap"$Route)

        val result = route(application, request).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }

    testInvalidJourneyType($packageName;format="cap"$, $className;format="decap"$Route)
    testNoUserAnswersError($className;format="decap"$Route)
  }
}
