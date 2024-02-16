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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.VatNumber
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.EnterVatNumberPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.Future

class BusinessPartnersSoleProprietorsVatRegistrationNumberControllerSpec
    extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockSessionCache: SessionRepository = mock[SessionRepository]
  val index: Int = 1

  val controller =
    new BusinessPartnersSoleProprietorsVatRegistrationNumberController(
      commonDependencies,
      views,
      mockActions,
      mockAppConfig,
      mockSessionCache
    )(mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>
    val partnerAddressPage: String = routes.BusinessPartnersAddressController.load(index, mode).url
    val selfAssessmentUtrPage: String = routes.BusinessPartnersSoleProprietorUtrController.load(index, mode).url

    s"load when in $mode" should {
      "Render the businessPartnersVatRegistrationNumber page" when {
        "there is no page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
          val result = csrfAddToken(controller.load(index, mode))(request)

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Does the partner have a UK VAT registration number?")
          reset(mockActions)
        }

        "there are userAnswers with page data" in {
          val vatRegistrationNumber = VatNumber(
            true,
            Some("123456789")
          )
          val userAnswers = UserAnswers(testUserId)
            .set[VatNumber](EnterVatNumberPage(1), vatRegistrationNumber)
            .success
            .value

          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          setupUserAction()
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
          val result = csrfAddToken(controller.load(index, mode))(request)

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Does the partner have a UK VAT registration number?")
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "redirect to the correct page" when {
        "the form has no errors, yes is selected and vatnumber supplied" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", "true"),
              ("vatNumber_value", "123456789")
            )
            .withMethod("POST")

          val result = csrfAddToken(controller.next(index, mode))(request)

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(partnerAddressPage)
          reset(mockActions)
        }

        "the form has no errors and no is selected" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody("vatNumber_yesNo" -> "false")
            .withMethod("POST")

          val result = csrfAddToken(controller.next(index, mode))(request)

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(selfAssessmentUtrPage)
          reset(mockActions)
        }
      }

      "return an error" when {
        "no checkbox option is selected" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", ""),
              ("vatNumber_value", "")
            )
            .withMethod("POST")

          val result = csrfAddToken(controller.next(index, mode))(request)

          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(
            "Select whether the business has a VAT registration number")
          reset(mockActions)
        }

        "Yes option is selected but no VAT Number is entered" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", "true"),
              ("vatNumber_value", "")
            )
            .withMethod("POST")

          val result = csrfAddToken(controller.next(index, mode))(request)

          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(
            "Enter the VAT registration number")
          reset(mockActions)
        }
      }
    }
  }
}
