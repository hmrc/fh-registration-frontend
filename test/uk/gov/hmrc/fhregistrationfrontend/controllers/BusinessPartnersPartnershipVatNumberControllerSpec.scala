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
import models.UserAnswers
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import play.api.mvc.Cookie
import uk.gov.hmrc.fhregistrationfrontend.forms.models.VatNumber
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.EnterVatNumberPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository

import scala.concurrent.Future

class BusinessPartnersPartnershipVatNumberControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockSessionCache: SessionRepository = mock[SessionRepository]
  val index: Int = 1

  val controller =
    new BusinessPartnersPartnershipVatNumberController(
      commonDependencies,
      views,
      mockActions,
      mockAppConfig,
      mockSessionCache
    )(mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>
    val partnershipUtrPage: String = routes.BusinessPartnersPartnershipUtrController.load().url
    val partnershipRegisteredAddressPage: String =
      routes.BusinessPartnersPartnershipRegisteredAddressController.load(index, mode).url

    s"load when in $mode" should {
      "Render the businessPartnersPartnershipVatNumber page" when {
        "there is no page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)

          setupUserAction()
          when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Does the partner have a UK VAT registration number?")
          page.getElementById("vatNumber_value").hasAttr("value") mustBe false
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

          setupDataRequiredAction(userAnswers)

          setupUserAction()
          when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Does the partner have a UK VAT registration number?")
          page.getElementById("vatNumber_value").attr("value") mustBe "123456789"
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "redirect to the Partnership SA UTR page" when {
        "Yes is selected and Vat Number supplied, and legal entity type is Partnership" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)

          when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "partnership"))
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", "true"),
              ("vatNumber_value", "123456789")
            )
            .withMethod("POST")

          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(partnershipUtrPage)
          reset(mockActions)
        }

        "No is selected, and legal entity type is Partnership" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)

          when(mockAppConfig.getRandomBusinessType()).thenReturn("partnership")
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "partnership"))
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", "false")
            )
            .withMethod("POST")

          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(partnershipUtrPage)
          reset(mockActions)
        }

        "No is selected, and legal entity type is Limited Liability Partnership" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)

          when(mockAppConfig.getRandomBusinessType()).thenReturn("limited-liability-partnership")
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership"))
            .withFormUrlEncodedBody("vatNumber_yesNo" -> "false")
            .withMethod("POST")

          val result = await(csrfAddToken(controller.next(index, mode))(request))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(partnershipUtrPage)
          reset(mockActions)
        }
      }

      "redirect to the Partnership Registered Office Address page" when {
        "Yes is selected and Vat Number supplied, and legal entity type is Limited Liability Partnership" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)

          when(mockAppConfig.getRandomBusinessType()).thenReturn("limited-liability-partnership")
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership"))
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", "true"),
              ("vatNumber_value", "123456789")
            )
            .withMethod("POST")

          val result = await(csrfAddToken(controller.next(index, mode))(request))
          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(partnershipRegisteredAddressPage)
          reset(mockActions)
        }
      }

      "Return the correct error" when {
        "the user selects yes but doesn't enter a VAT number" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)

          when(mockAppConfig.getRandomBusinessType()).thenReturn("limited-liability-partnership")
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership"))
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", "true"),
              ("vatNumber_value", "")
            )
            .withMethod("POST")

          val result = await(csrfAddToken(controller.next(index, mode))(request))
          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Does the partner have a UK VAT registration number?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(
            "Enter the VAT registration number")
          reset(mockActions)
        }

        "the user doesn't select an option" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers)

          when(mockAppConfig.getRandomBusinessType()).thenReturn("limited-liability-partnership")
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership"))
            .withFormUrlEncodedBody(
              ("vatNumber_yesNo", ""),
              ("vatNumber_value", "")
            )
            .withMethod("POST")

          val result = await(csrfAddToken(controller.next(index, mode))(request))
          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Does the partner have a UK VAT registration number?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(
            "Select whether the business has a VAT registration number")
          reset(mockActions)
        }
      }
    }
  }
}
