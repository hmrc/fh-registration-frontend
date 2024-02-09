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
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.BusinessPartnersEnterAddress
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.EnterAddressPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.Future

class BusinessPartnersEnterAddressControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockSessionCache: SessionRepository = mock[SessionRepository]
  val index: Int = 1
  val checkYourAnswersPage: String = routes.BusinessPartnersCheckYourAnswersController.load().url

  val controller =
    new BusinessPartnersEnterAddressController(commonDependencies, views, mockActions, mockAppConfig, mockSessionCache)(
      mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the business partner enter address page" when {
        "the new business partner pages are enabled and there is no page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("Enter the partner’s address?")
          reset(mockActions)
        }

        "the new business partner pages are enabled and there are userAnswers with page data" in {
          val address = BusinessPartnersEnterAddress(
            addressLine1 = "23 High Street",
            addressLine2 = Some("Park View"),
            addressLine3 = ("Gloucester"),
            postcode = Some("NE98 1ZZ"))
          val userAnswers = UserAnswers(testUserId)
            .set[BusinessPartnersEnterAddress](EnterAddressPage(1), address)
            .success
            .value
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("Enter the partner’s address?")
          val line1Field = page.getElementById("enterAddress.line1")
          val line2Field = page.getElementById("enterAddress.line2")
          val line3Field = page.getElementById("enterAddress.line3")
          val postcodeField = page.getElementById("enterAddress.postcode")
          line1Field.attr("value") should include("23 High Street")
          line2Field.attr("value") should include("Park View")
          line3Field.attr("value") should include("Gloucester")
          postcodeField.attr("value") should include("NE98 1ZZ")
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "redirect to the Check Your Answers page" when {
        "the new business partner pages are enabled" should {
          "all address fields are populated" in {
            val userAnswers = UserAnswers(testUserId)
            setupDataRequiredActionBusinessPartners(userAnswers, mode)
            when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
            when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
            val request = FakeRequest()
              .withFormUrlEncodedBody(
                "enterAddress.line1"    -> "1",
                "enterAddress.line2"    -> "Old Town",
                "enterAddress.line3"    -> "Cityville",
                "enterAddress.postcode" -> "AA1 2YZ"
              )
              .withMethod("POST")
            val result = await(csrfAddToken(controller.next(index, mode))(request))

            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get should include(checkYourAnswersPage)
            reset(mockActions)
          }

          "only mandatory address fields are populated" in {
            val userAnswers = UserAnswers(testUserId)
            setupDataRequiredActionBusinessPartners(userAnswers, mode)
            when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
            when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

            val request = FakeRequest()
              .withFormUrlEncodedBody(
                "enterAddress.line1" -> "1",
                "enterAddress.line3" -> "Cityville"
              )
              .withMethod("POST")
            val result = await(csrfAddToken(controller.next(index, mode))(request))

            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get should include(checkYourAnswersPage)
            reset(mockActions)
          }
        }
      }

      "return a 400" when {
        "mandatory fields are not populated" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "enterAddress.line1" -> "",
              "enterAddress.line3" -> ""
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Enter the partner’s address?")
          page.getElementsByClass("govuk-list govuk-error-summary__list").text() should include(
            "You must enter line 1 of the address You must enter the Town or City of the address")
          reset(mockActions)
        }
      }
    }
  }
}
