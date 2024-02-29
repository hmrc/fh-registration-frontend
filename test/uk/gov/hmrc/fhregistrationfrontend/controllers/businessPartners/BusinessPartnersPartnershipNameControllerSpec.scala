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
import uk.gov.hmrc.fhregistrationfrontend.controllers.ControllerSpecWithGuiceApp
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnershipNamePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.Future

class BusinessPartnersPartnershipNameControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockSessionCache: SessionRepository = mock[SessionRepository]
  val index = 1
  val controller =
    new BusinessPartnersPartnershipNameController(commonDependencies, views, mockActions, mockSessionCache)(mockMcc)

  val PartnershipNameTitle = "What is the name of the partnership? - Business partners"
  val pageNotFoundTitle = "Page not found"

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the business partner partnership name page" when {
        "there is useranswer but no page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          val request = FakeRequest()
          val result = csrfAddToken(controller.load(index, mode))(request)

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include(PartnershipNameTitle)
          reset(mockActions)
        }

        "there are userAnswers with page data" in {
          val partnerName = "test"
          val userAnswers = UserAnswers(testUserId)
            .set[String](PartnershipNamePage(1), partnerName)
            .success
            .value
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          val request = FakeRequest()
          val result = csrfAddToken(controller.load(index, mode))(request)

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include(PartnershipNameTitle)
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "redirect to the trading name page and save the answer to database" when {
        "the user answers doesn't contain page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "partnershipName" -> "Partnership name goes here"
            )
            .withMethod("POST")
          val result = csrfAddToken(controller.next(index, mode))(request)

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(
            routes.BusinessPartnersPartnershipTradingNameController.load(1, mode).url.drop(6))
          reset(mockActions)
        }
      }
    }
  }
}
