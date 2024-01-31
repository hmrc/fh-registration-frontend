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
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, UkAddressLookup}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.UkAddressLookupPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.Future

class BusinessPartnersPartnershipConfirmRegisteredAddressControllerSpec
    extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockSessionCache: SessionRepository = mock[SessionRepository]
  val emptyUkAddressLookup = UkAddressLookup(Some("44 test lane"), "SW1A 2AA", Map.empty)
  val singleUkAddressLookup = UkAddressLookup(
    Some("44 test lane"),
    "SW1A 2AA",
    Map("1" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, None)))
  val multipleAddressLookup = UkAddressLookup(
    Some("44 test lane"),
    "SW1A 2AA",
    Map(
      "1" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, None),
      "2" -> Address("45 test lane", None, None, None, "SW1A 2AB", None, None))
  )

  val controller =
    new BusinessPartnersPartnershipConfirmRegisteredAddressController(
      commonDependencies,
      views,
      mockActions,
      mockSessionCache)(mockMcc)

  val pageTitle: String = "Confirm the partnership’s registered office address?"
  val checkYourAnswersPage = routes.BusinessPartnersCheckYourAnswersController.load().url

  val index = 1
  def createUserAnswers(answers: UkAddressLookup): UserAnswers =
    UserAnswers(testUserId)
      .set[UkAddressLookup](UkAddressLookupPage(1), answers)
      .success
      .value

  List(NormalMode, CheckMode).foreach { mode =>
    val registeredAddressPage: String =
      routes.BusinessPartnersPartnershipRegisteredAddressController.load(index, mode).url
    val enterAddressUrl: String = routes.BusinessPartnersPartnershipEnterAddressController.load(index, mode).url

    s"load when in $mode" should {
      "Render the confirm address page" when {
        "a single address is found in user answers" in {
          val userAnswers = createUserAnswers(singleUkAddressLookup)
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include(pageTitle)
          page.getElementsByTag("h1").text should include("Confirm the company’s registered office address")
          page.body.text should include("44 test lane")
          page.getElementById("confirm-edit").attr("href") should include(enterAddressUrl)
          reset(mockActions)
        }
      }

      "Redirect to Registered Address Page" when {
        "address lookup address list is empty" in {
          val userAnswers = createUserAnswers(emptyUkAddressLookup)
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(registeredAddressPage)
          reset(mockActions)
        }

        "address lookup address list contains more than one address" in {
          val userAnswers = createUserAnswers(multipleAddressLookup)
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(registeredAddressPage)
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "Redirect to Check your answers page" when {
        "the use clicks save and continue" in {
          val userAnswers = createUserAnswers(singleUkAddressLookup)
          setupDataRequiredAction(userAnswers, mode)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(checkYourAnswersPage)
          reset(mockActions)
        }
      }

      "Redirect to the Business Partners Address page" when {
        "addressList contains no addresses" in {
          val userAnswers = createUserAnswers(emptyUkAddressLookup)
          setupDataRequiredAction(userAnswers, mode)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(registeredAddressPage)
          reset(mockActions)
        }

        "addressList cache contains a multiple addresses" in {
          val userAnswers = createUserAnswers(multipleAddressLookup)
          setupDataRequiredAction(userAnswers, mode)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result) shouldBe Some(registeredAddressPage)
          reset(mockActions)
        }
      }
    }
  }
}
