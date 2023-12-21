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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import com.codahale.metrics.SharedMetricRegistries
import models.{CheckMode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.Mockito.{reset, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

class BusinessPartnersConfirmAddressControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]

  val mockAppConfig = mock[FrontendAppConfig]

  val controller =
    new BusinessPartnersConfirmAddressController(commonDependencies, views, mockActions, mockAppConfig)(mockMcc)
  val backLink = "/fhdds/business-partners/partner-address"
  val enterAddressLink = "/fhdds/business-partners/enter-partner-address"
  val index = 1
  val userAnswers = UserAnswers(testUserId)

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {

      "Render the confirm address page" when {
        "the new business partner pages are enabled" in {
          setupDataRequiredAction(userAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Confirm the partner’s address?")
          // should be mocked out when Save4Later changes included
          page.body.text should include("Confirm test business partner’s address")
          page.body.text should include("1 Romford Road")
          page.getElementById("confirm-edit").attr("href") should include(enterAddressLink)
          page.getElementsByClass("govuk-back-link").attr("href") should include(backLink)
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "return 200" when {
        "the new business partner pages are enabled and the user clicks 'save and continue' button" in {
          setupDataRequiredAction(userAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(
            routes.BusinessPartnersCheckYourAnswersController.load().url.drop(6))
          reset(mockActions)
        }
      }
    }
  }
}
