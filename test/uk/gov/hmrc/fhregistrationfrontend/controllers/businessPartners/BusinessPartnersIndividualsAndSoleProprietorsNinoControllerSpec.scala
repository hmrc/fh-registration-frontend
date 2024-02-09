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
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.controllers.ControllerSpecWithGuiceApp
import uk.gov.hmrc.fhregistrationfrontend.forms.models.NationalInsuranceNumber
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.IndividualsAndSoleProprietorsNinoPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.RadioHelper

import scala.concurrent.Future

class BusinessPartnersIndividualsAndSoleProprietorsNinoControllerSpec
    extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  lazy val radioHelper: RadioHelper = app.injector.instanceOf[RadioHelper]
  lazy val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockSessionCache: SessionRepository = mock[SessionRepository]
  val vatRegNumPage: String =
    routes.BusinessPartnersSoleProprietorsVatRegistrationNumberController.load(1, NormalMode).url
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
          setupDataRequiredAction(userAnswers, mode)

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
          setupDataRequiredAction(userAnswers, mode)

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
        setupDataRequiredAction(userAnswers, mode)
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
        val expectedLocation = if (mode == NormalMode) {
          "/business-partners/partner-type/1"
        } else {
          "/business-partners/change-partner-type/1"
        }
        redirectLocation(result).get should include(expectedLocation)
        reset(mockActions)
      }

      "redirect to VAT number page" when {
        "the Yes radio button is selected and a NINO is entered" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers, mode)
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
          redirectLocation(result).get should include(vatRegNumPage)
          reset(mockActions)
        }
      }

      "return 400" when {
        "a radio button is not selected" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers, mode)
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
