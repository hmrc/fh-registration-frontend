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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.models.TradingName
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.SoleProprietorsTradingNamePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import org.scalatest.TryValues.convertTryToSuccessOrFailure

import scala.concurrent.Future

class BusinessPartnersSoleProprietorsTradingNameControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]
  lazy val mockAppConfig = mock[FrontendAppConfig]
  lazy val mockSession = mock[SessionRepository]
  val index = 1

  val controller =
    new BusinessPartnersSoleProprietorsTradingNameController(commonDependencies, views, mockActions, mockSession)(
      mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the business partner trading name page" when {
        "no user answers supplied" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include(
            "Does the partner’s business use a trading name that is different from its registered name?")
          reset(mockActions)
        }

        "user answers supplied" in {
          val tradingName = TradingName(true, Some("partner trading name"))
          val userAnswers = UserAnswers(testUserId)
            .set[TradingName](SoleProprietorsTradingNamePage(index), tradingName)
            .success
            .value
          setupDataRequiredAction(userAnswers, mode)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include(
            "Does the partner’s business use a trading name that is different from its registered name?")
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "redirect to the Business Partners National Insurance Number page" in {
        val userAnswers = UserAnswers(testUserId)
        setupDataRequiredAction(userAnswers, mode)

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockSession.set(any())).thenReturn(Future.successful(true))
        val request = FakeRequest()
          .withFormUrlEncodedBody(
            "tradingName_yesNo" -> "true",
            "tradingName_value" -> "Blue Peter"
          )
          .withMethod("POST")
        val result = await(csrfAddToken(controller.next(index, mode))(request))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get should include(
          routes.BusinessPartnersIndividualsAndSoleProprietorsNinoController.load(index, mode).url)
        reset(mockActions)
      }

      "return 400 when supplied with empty form data" in {
        val userAnswers = UserAnswers(testUserId)
        setupDataRequiredAction(userAnswers, mode)

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockSession.set(any())).thenReturn(Future.successful(true))
        val request = FakeRequest()
          .withFormUrlEncodedBody(
            "tradingName_yesNo" -> "",
            "tradingName_value" -> ""
          )
          .withMethod("POST")
        val result = await(csrfAddToken(controller.next(index, mode))(request))

        status(result) shouldBe BAD_REQUEST
        reset(mockActions)
      }
    }
  }
}
