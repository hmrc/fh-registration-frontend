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
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.Mockito.{reset, when}
import org.mockito.ArgumentMatchers.any
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.CompanyRegistrationNumber
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.CompanyRegistrationNumberPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import scala.concurrent.Future

class BusinessPartnersPartnershipCompanyRegistrationNumberControllerSpec
    extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  lazy val mockSessionCache: SessionRepository = mock[SessionRepository]

  val controller =
    new BusinessPartnersPartnershipCompanyRegistrationNumberController(
      commonDependencies,
      views,
      mockActions,
      mockSessionCache)(mockMcc)
  val index = 1

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the Company Registration page" when {
        "there is no page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership"))
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("What is the partnership’s company registration number?")
          reset(mockActions)
        }

        "there is page data" in {
          val crn = CompanyRegistrationNumber("01234567", None)
          val userAnswers = UserAnswers(testUserId)
            .set[CompanyRegistrationNumber](CompanyRegistrationNumberPage(index), crn)
            .success
            .value
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership"))
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("What is the partnership’s company registration number?")
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "redirect to the Partnership VAT Reg Number page" when {
        "the form has no errors and companyRegistrationNumber supplied" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers, mode)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership"))
            .withFormUrlEncodedBody(("companyRegistrationNumber", "01234567"))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(
            routes.BusinessPartnersPartnershipVatNumberController.load(1, mode).url)
          reset(mockActions)
        }
      }

      "return a BadRequest" when {
        "the user doesn't enter a company registration number" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
            .withCookies(Cookie("businessType", "limited-liability-partnership"))
            .withFormUrlEncodedBody(("companyRegistrationNumber", ""))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("What is the partnership’s company registration number?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(
            "Enter the company registration number")
          reset(mockActions)
        }
      }
    }
  }
}
