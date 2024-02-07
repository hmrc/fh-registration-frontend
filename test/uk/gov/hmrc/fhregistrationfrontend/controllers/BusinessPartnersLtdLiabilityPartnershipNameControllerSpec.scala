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
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.models.LtdLiabilityPartnershipName
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.LimitedLiabilityPartnershipNamePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.Future

class BusinessPartnersLtdLiabilityPartnershipNameControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockSessionCache: SessionRepository = mock[SessionRepository]
  val controller: BusinessPartnersLtdLiabilityPartnershipNameController =
    new BusinessPartnersLtdLiabilityPartnershipNameController(commonDependencies, views, mockActions, mockSessionCache)(
      mockMcc)

  val pageHeading: String = "Enter the name of the limited liability partnership"
  val pageTitle: String = "What is the name of the limited liability partnership?"
  val pageNotFoundTitle: String = "Page not found"
  val index: Int = 1
  val userAnswers: UserAnswers = UserAnswers(testUserId)
  val userAnswersWithPageData: UserAnswers = UserAnswers(testUserId)
    .set[LtdLiabilityPartnershipName](
      LimitedLiabilityPartnershipNamePage(1),
      LtdLiabilityPartnershipName("Test Name Ltd"))
    .success
    .value

  List(NormalMode, CheckMode).foreach { mode =>
    val tradingNamePage: String = routes.BusinessPartnersPartnershipTradingNameController.load(index, mode).url.drop(6)

    s"load when in $mode" should {
      "Render the Limited Liability Partnership Name page" when {
        "there are useranswers but no page data" in {
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include(pageTitle)
          page.getElementById("ltdLiabilityPartnershipName").hasAttr("value") mustBe false
          reset(mockActions)
        }

        "there are useranswers with page data" in {
          setupDataRequiredActionBusinessPartners(userAnswersWithPageData, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include(pageTitle)
          page.getElementById("ltdLiabilityPartnershipName").attr("value") must include("Test Name Ltd")
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "redirect to the Trading Name page" when {
        "the form has no errors and limited liability partnership name is supplied" in {
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
          val request = FakeRequest()
            .withFormUrlEncodedBody(("ltdLiabilityPartnershipName", "Partnership Name"))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(tradingNamePage)
          reset(mockActions)
        }
      }

      "return an error" when {
        "the user doesn't enter a limited liability partnership name" in {
          setupDataRequiredActionBusinessPartners(userAnswers, mode)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
          val request = FakeRequest()
            .withFormUrlEncodedBody(("ltdLiabilityPartnershipName", ""))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include(pageTitle)
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(pageHeading)
          reset(mockActions)
        }
      }
    }
  }
}
