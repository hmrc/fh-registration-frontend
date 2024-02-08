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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Utr
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.SoleProprietorUtrPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.Future

class BusinessPartnersSoleProprietorUtrControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]

  val mockAppConfig = mock[FrontendAppConfig]
  lazy val mockSession = mock[SessionRepository]
  val index = 1

  val controller =
    new BusinessPartnersSoleProprietorUtrController(commonDependencies, views, mockActions, mockSession)(mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the SoleProprietor Utr page" when {
        "no user answers supplied" in {

          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("What is the partner’s Corporation Tax Unique Taxpayer Reference (UTR)?")
          reset(mockActions)
        }

        "user answers supplied" in {
          val userAnswers =
            UserAnswers(testUserId).set[Utr](SoleProprietorUtrPage(index), Utr("1234567890")).success.value
          setupDataRequiredActionBusinessPartners(userAnswers, mode)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("What is the partner’s Corporation Tax Unique Taxpayer Reference (UTR)?")
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" when {
      "redirect to the Partner Address page" in {
        val userAnswers = UserAnswers(testUserId)
        setupDataRequiredActionBusinessPartners(userAnswers, mode)

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockSession.set(any())).thenReturn(Future.successful(true))
        val request = FakeRequest()
          .withFormUrlEncodedBody(
            ("uniqueTaxpayerReference_yesNo", "true"),
            ("uniqueTaxpayerReference_value", "1234567890"))
          .withMethod("POST")
        val result = await(csrfAddToken(controller.next(index, mode))(request))

        status(result) shouldBe SEE_OTHER

        redirectLocation(result).get should include(routes.BusinessPartnersAddressController.load(index, mode).url)
        reset(mockActions)
      }

      "return 400 when supplied with empty form data" in {
        val userAnswers = UserAnswers(testUserId)
        setupDataRequiredActionBusinessPartners(userAnswers, mode)

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        when(mockSession.set(any())).thenReturn(Future.successful(true))
        val request = FakeRequest()
          .withFormUrlEncodedBody(
            "uniqueTaxpayerReference_yesNo" -> "",
            "uniqueTaxpayerReference_value" -> ""
          )
          .withMethod("POST")
        val result = await(csrfAddToken(controller.next(index, mode))(request))

        status(result) shouldBe BAD_REQUEST
        reset(mockActions)
      }
    }
  }
}
