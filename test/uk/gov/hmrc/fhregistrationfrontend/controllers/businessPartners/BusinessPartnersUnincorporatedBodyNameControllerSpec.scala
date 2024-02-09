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

package uk.gov.hmrc.fhregistrationfrontend.controllers.businessPartners

import com.codahale.metrics.SharedMetricRegistries
import models.{CheckMode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.controllers.ControllerSpecWithGuiceApp
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.UnincorporatedBodyName
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.UnincorporatedBodyNamePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.Future

class BusinessPartnersUnincorporatedBodyNameControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockSessionCache: SessionRepository = mock[SessionRepository]
  val controller =
    new BusinessPartnersUnincorporatedBodyNameController(commonDependencies, views, mockActions, mockSessionCache)(
      mockMcc)
  val unincorpoBodyTradingNameUrl: String = routes.BusinessPartnersUnincorporatedBodyTradingNameController.load().url

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the Unincorporated Body Name page" when {
        "there is no page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers, mode)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(1, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("What is the unincorporated body name?")
          reset(mockActions)
        }

        "there is page data" in {
          val unincorporatedBodyName = UnincorporatedBodyName("unincorporated body name")
          val userAnswers = UserAnswers(testUserId)
            .set[UnincorporatedBodyName](UnincorporatedBodyNamePage(1), unincorporatedBodyName)
            .success
            .value
          setupDataRequiredAction(userAnswers, mode)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(1, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("What is the unincorporated body name?")
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "redirect to the Unincorporated Body Trading Name page" in {
        val userAnswers = UserAnswers(testUserId)
        setupDataRequiredAction(userAnswers, mode)
        when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

        val request = FakeRequest()
          .withFormUrlEncodedBody(
            ("unincorporatedBodyName_value", "Test Body")
          )
          .withMethod("POST")
        val result = await(csrfAddToken(controller.next(1, mode))(request))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get should include(unincorpoBodyTradingNameUrl)
        reset(mockActions)
      }

      "the user doesn't enter a unincorporated body name" in {
        val userAnswers = UserAnswers(testUserId)
        setupDataRequiredAction(userAnswers, mode)
        when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

        val request = FakeRequest()
          .withFormUrlEncodedBody(("unincorporatedBodyName_value", ""))
          .withMethod("POST")
        val result = await(csrfAddToken(controller.next(1, mode))(request))

        status(result) shouldBe BAD_REQUEST
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("What is the unincorporated body name?")
        page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(
          "Enter an unincorporated body name")
        reset(mockActions)
      }
    }
  }
}
