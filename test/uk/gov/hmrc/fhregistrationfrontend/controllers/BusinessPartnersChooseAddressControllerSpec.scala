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
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.controllers.routes._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, ChooseAddress, UkAddressLookup}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.{AddressPage, UkAddressLookupPage}
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository

import scala.concurrent.Future

class BusinessPartnersChooseAddressControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockSessionCache: SessionRepository = mock[SessionRepository]
  val index: Int = 1

  val controller = new BusinessPartnersChooseAddressController(
    commonDependencies,
    views,
    mockActions,
    mockAppConfig,
    mockSessionCache)(mockMcc)

  def createUserAnswers(answers: UkAddressLookup): UserAnswers =
    UserAnswers(testUserId)
      .set[UkAddressLookup](UkAddressLookupPage(1), answers)
      .success
      .value

  val emptyUserAnswers: UserAnswers = UserAnswers(testUserId)

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the choose address page" when {
        "addressList cache returns multiple addresses" in {
          val cachedUkAddressLookup = UkAddressLookup(
            Some("44 test lane"),
            "SW1A 2AA",
            Map(
              "1" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, None),
              "2" -> Address("77 test lane", None, None, None, "SW1A 2AA", None, None)
            )
          )

          val userAnswers = createUserAnswers(cachedUkAddressLookup)
          setupDataRequiredAction(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Choose address")
          reset(mockActions)
        }
      }

      "Redirect to the Business Partners Address page" when {
        "addressList cache contains an empty address" in {
          val cachedUkAddressLookup = UkAddressLookup(
            Some("44 test lane"),
            "SW1A 2AA",
            Map.empty
          )

          val userAnswers = createUserAnswers(cachedUkAddressLookup)
          setupDataRequiredAction(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.BusinessPartnersAddressController.load(1, mode).url)
          reset(mockActions)
        }

        "addressList cache contains a single address" in {
          val cachedUkAddressLookup = UkAddressLookup(
            Some("44 test lane"),
            "SW1A 2AA",
            Map(
              "1" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, None)
            )
          )

          val userAnswers = createUserAnswers(cachedUkAddressLookup)
          setupDataRequiredAction(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.BusinessPartnersAddressController.load(1, mode).url)
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "redirect to the Check Your Answers page" when {
        "user selects a value and submits the form" in {
          val cachedUkAddressLookup = UkAddressLookup(
            Some("44 test lane"),
            "SW1A 2AA",
            Map(
              "1" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, None),
              "2" -> Address("77 test lane", None, None, None, "SW1A 2AA", None, None)
            )
          )

          val userAnswers = createUserAnswers(cachedUkAddressLookup)
          setupDataRequiredAction(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody("chosenAddress" -> "1")
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.BusinessPartnersCheckYourAnswersController.load().url)
          reset(mockActions)
        }
      }

      "Redirect to the Business Partners Address page" when {
        "addressList cache contains an empty address" in {
          val cachedUkAddressLookup = UkAddressLookup(
            Some("44 test lane"),
            "SW1A 2AA",
            Map.empty
          )

          val userAnswers = createUserAnswers(cachedUkAddressLookup)
          setupDataRequiredAction(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody("chosenAddress" -> "1")
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.BusinessPartnersAddressController.load(index, mode).url)
          reset(mockActions)
        }

        "addressList cache contains a single address" in {
          val cachedUkAddressLookup = UkAddressLookup(
            Some("44 test lane"),
            "SW1A 2AA",
            Map(
              "1" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, None)
            )
          )

          val userAnswers = createUserAnswers(cachedUkAddressLookup)
          setupDataRequiredAction(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody("chosenAddress" -> "1")
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(routes.BusinessPartnersAddressController.load(index, mode).url)
          reset(mockActions)
        }
      }

      "return a bad request" when {
        "the user does not select a value" in {
          val cachedUkAddressLookup = UkAddressLookup(
            Some("44 test lane"),
            "SW1A 2AA",
            Map(
              "1" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, None),
              "2" -> Address("77 test lane", None, None, None, "SW1A 2AA", None, None)
            )
          )

          val userAnswers = createUserAnswers(cachedUkAddressLookup)
          setupDataRequiredAction(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody("chosenAddress" -> "")
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe BAD_REQUEST
          reset(mockActions)
        }
      }
    }
  }
}
