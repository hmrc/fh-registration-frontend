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
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.actions.JourneyRequestBuilder
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessType, PartnerName}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.IndividualsAndSoleProprietorsPartnerNamePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.RadioHelper
import uk.gov.hmrc.fhregistrationfrontend.views.{Mode, Views}
import org.scalatest.TryValues
import org.scalatest.TryValues.convertTryToSuccessOrFailure

import scala.concurrent.Future

class BusinessPartnersIndividualsAndSoleProprietorsPartnerNameControllerSpec
    extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]
  lazy val radioHelper = app.injector.instanceOf[RadioHelper]
  lazy val mockAppConfig = mock[FrontendAppConfig]
  val mockSessionCache = mock[SessionRepository]
  val index = 1

  val controller =
    new BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController(
      commonDependencies,
      views,
      mockActions,
      mockAppConfig,
      mockSessionCache)(mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the business partner partner's name page" when {

        "The business partner v2 pages are enabled and there is no page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include(
            "What is the name of the partner? - Business partners - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK")
          reset(mockActions)
        }

        "The business partner v2 pages are enabled and there are userAnswers with page data" in {
          val partnerName = PartnerName("test", "user")
          val userAnswers = UserAnswers(testUserId)
            .set[PartnerName](IndividualsAndSoleProprietorsPartnerNamePage(1), partnerName)
            .success
            .value
          setupDataRequiredAction(userAnswers)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include(
            "What is the name of the partner? - Business partners - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK")
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "return 200" when {
        "The business partner v2 pages are enabled" should {
          "business type is neither Sole Proprietor or Individual" in {
            val userAnswers = UserAnswers(testUserId)
            setupDataRequiredAction(userAnswers)
            when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
            when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
            val request = FakeRequest()
              .withFormUrlEncodedBody(
                "firstName" -> "first name",
                "lastName"  -> "last name"
              )
              .withMethod("POST")
            val result = await(csrfAddToken(controller.next(index, mode))(request))

            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get should include("/business-partners")
            reset(mockActions)
          }
        }
      }

      "redirect to Business Partner NINO page" when {
        "business type is Individual" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAppConfig.getRandomBusinessType).thenReturn("individual")
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "individual"))
            .withFormUrlEncodedBody(
              "firstName" -> "first name",
              "lastName"  -> "last name"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include("/business-partners/partner-national-insurance-number")
          reset(mockActions)
        }
      }

      "redirect to Business Partner Trading Name page" when {
        "business type is Sole Proprietor" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAppConfig.getRandomBusinessType).thenReturn("sole-proprietor")
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "sole-proprietor"))
            .withFormUrlEncodedBody(
              "firstName" -> "first name",
              "lastName"  -> "last name"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include("/business-partners/partner-trading-name")
          reset(mockActions)
        }
      }
    }
  }
}
