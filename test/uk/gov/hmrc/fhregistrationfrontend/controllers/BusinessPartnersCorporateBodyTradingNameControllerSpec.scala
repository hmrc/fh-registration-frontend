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
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.TradingName
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.CorporateBodyTradingNamePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.Future

class BusinessPartnersCorporateBodyTradingNameControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  lazy val mockSession = mock[SessionRepository]
  val index = 1

  val companyRegNumUrl: String = routes.BusinessPartnersCorporateBodyCompanyRegistrationNumberController.load().url

  val controller =
    new BusinessPartnersCorporateBodyTradingNameController(commonDependencies, views, mockActions, mockSession)(mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the corporate body trading name page" when {
        "without page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include(
            "Does the corporate body use a trading name that is different from its registered name? - Business partners")
          reset(mockActions)
        }

        "with page data" in {
          val tradingName = TradingName(hasValue = true, Some("partner trading name"))
          val userAnswers = UserAnswers(testUserId)
            .set[TradingName](CorporateBodyTradingNamePage(index), tradingName)
            .success
            .value
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include(
            "Does the corporate body use a trading name that is different from its registered name? - Business partners")
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" when {
      "Redirects to Corporate Body Company Registration Number page" when {
        "the user clicks yes and supplies an updated trading name" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers, mode)

          when(mockSession.set(any())).thenReturn(Future.successful(true))
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "tradingName_yesNo" -> "true",
              "tradingName_value" -> "Alfie Limited"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(companyRegNumUrl)
          reset(mockActions)
        }

        "the user clicks no and submits form" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers, mode)

          when(mockSession.set(any())).thenReturn(Future.successful(true))
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "tradingName_yesNo" -> "false",
              "tradingName_value" -> ""
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(companyRegNumUrl)
          reset(mockActions)
        }
      }

      "return 400 when supplied with empty form data" in {
        val userAnswers = UserAnswers(testUserId)
        setupDataRequiredAction(userAnswers, mode)

        when(mockSession.set(any())).thenReturn(Future.successful(true))
        val request = FakeRequest()
          .withFormUrlEncodedBody(
            "tradeName_yesNo" -> "",
            "tradeName_value" -> ""
          )
          .withMethod("POST")
        val result = await(csrfAddToken(controller.next(index, mode))(request))

        status(result) shouldBe BAD_REQUEST
        reset(mockActions)
      }
    }
  }
}
