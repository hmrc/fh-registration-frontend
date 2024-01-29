/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.fhregistrationfrontend.controllers.companyOfficers

import com.codahale.metrics.SharedMetricRegistries
import models.NormalMode
import org.jsoup.Jsoup
import org.mockito.Mockito.{reset, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.controllers.ControllerSpecWithGuiceApp
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

class CompanyOfficersCYAControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]

  val mockAppConfig = mock[FrontendAppConfig]

  val controller =
    new CompanyOfficerCYAController(commonDependencies, views, mockActions, mockAppConfig)(mockMcc)

  "load" should {
    "Render the Company Officers Check Your Answers page" when {
      "user answers exist" in {
        setupDataRequiredAction(emptyUserAnswers, NormalMode)
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load(1))(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title should include("Check your answers")
        reset(mockActions)
      }
    }
  }

  "next" when {
    "the new Business Partner pages are enabled" should {
      "return 200" when {
        "the use clicks save and continue" in {
          setupDataRequiredAction(emptyUserAnswers, NormalMode)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.next(1))(request))

          status(result) shouldBe OK
          contentAsString(result) shouldBe "Form submitted, with result:"
          reset(mockActions)
        }
      }
    }
  }
}
