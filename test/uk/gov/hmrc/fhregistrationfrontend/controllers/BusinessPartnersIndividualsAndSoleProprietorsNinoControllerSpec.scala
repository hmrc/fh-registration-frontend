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
import org.jsoup.Jsoup
import org.mockito.Mockito.{reset, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.RadioHelper
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import models.{CheckMode, NormalMode, UserAnswers}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.NationalInsuranceNumber
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.IndividualsAndSoleProprietorsNinoPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.mockito.ArgumentMatchers.any
import scala.concurrent.Future
import play.api.mvc.Cookie

class BusinessPartnersIndividualsAndSoleProprietorsNinoControllerSpec
    extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]
  lazy val radioHelper = app.injector.instanceOf[RadioHelper]
  lazy val mockAppConfig = mock[FrontendAppConfig]
  val mockSessionCache = mock[SessionRepository]
  val index = 1

  val controller =
    new BusinessPartnersIndividualsAndSoleProprietorsNinoController(
      radioHelper,
      commonDependencies,
      views,
      mockActions,
      mockAppConfig,
      mockSessionCache)(mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the business partner nino page" when {

        "The business partner v2 pages are enabled and there is no page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("Does the partner have a National Insurance number?")
          reset(mockActions)
        }

        "The business partner v2 pages are enabled and there are userAnswers with page data" in {
          val nino = NationalInsuranceNumber(true, Some("AB123456C"))
          val userAnswers = UserAnswers(testUserId)
            .set[NationalInsuranceNumber](IndividualsAndSoleProprietorsNinoPage(1), nino)
            .success
            .value
          setupDataRequiredAction(userAnswers)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("Does the partner have a National Insurance number?")
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" when {

      "business type is neither Sole Proprietor or Individual" in {
        val userAnswers = UserAnswers(testUserId)
        setupDataRequiredAction(userAnswers)
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
        val request = FakeRequest()
          .withFormUrlEncodedBody(
            "nationalInsuranceNumber_yesNo" -> "true",
            "nationalInsuranceNumber_value" -> "QQ123456C"
          )
          .withMethod("POST")
        val result = await(csrfAddToken(controller.next(index, mode))(request))

        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get should include("/business-partners")
        reset(mockActions)
      }

      "redirect to VAT number page" when {
        "the Yes radio button is selected and a NINO is entered" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
          val request = FakeRequest()
            .withCookies(Cookie("businessType", "individual"))
            .withFormUrlEncodedBody(
              "nationalInsuranceNumber_yesNo" -> "true",
              "nationalInsuranceNumber_value" -> "QQ123456C"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(routes.BusinessPartnersVatRegistrationNumberController.load().url)
          reset(mockActions)
        }
      }

      "return 400" when {
        "a radio button is not selected" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
          val request = FakeRequest()
            .withCookies(Cookie("businessType", "individual"))
            .withFormUrlEncodedBody(
              "nationalInsuranceNumber_yesNo" -> "",
              "nationalInsuranceNumber_value" -> ""
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe BAD_REQUEST
          reset(mockActions)
        }
      }
    }
  }
}
