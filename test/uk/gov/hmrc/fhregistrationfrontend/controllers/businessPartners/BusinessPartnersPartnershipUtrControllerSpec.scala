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
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.HasUniqueTaxpayerReference
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnershipHasUtrPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.Future

class BusinessPartnersPartnershipUtrControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()
  val mockSessionCache = mock[SessionRepository]
  val index = 1
  override lazy val views: Views = app.injector.instanceOf[Views]

  val controller =
    new BusinessPartnersPartnershipUtrController(commonDependencies, views, mockActions, mockSessionCache)(mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the businessPartnersUtr page" when {
        "there is no page data" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          val request = FakeRequest()
          val result = csrfAddToken(controller.load(index, mode))(request)

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Does the partner have a Self Assessment Unique Taxpayer Reference (UTR)?")
          reset(mockActions)
        }

        "there is page data" in {
          val hasUtr = HasUniqueTaxpayerReference(hasValue = true, Some("1234567890"))
          val userAnswers = UserAnswers(testUserId)
            .set[HasUniqueTaxpayerReference](PartnershipHasUtrPage(1), hasUtr)
            .success
            .value
          setupDataRequiredActionBusinessPartners(userAnswers, mode)

          val request = FakeRequest()
          val result = csrfAddToken(controller.load(index, mode))(request)

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("Does the partner have a Self Assessment Unique Taxpayer Reference (UTR)?")
          reset(mockActions)
        }
      }

    }

    s"next when in $mode" should {
      val partnershipRegOfficeAddressUrl: String =
        routes.BusinessPartnersPartnershipRegisteredAddressController.load(index, mode).url
      "redirect to the Partnership Registered Office Address page" when {
        "the form has no errors, yes is selected and UTR supplied" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("uniqueTaxpayerReference_yesNo", "true"),
              ("uniqueTaxpayerReference_value", "1234567890"))
            .withMethod("POST")
          val result = csrfAddToken(controller.next(index, mode))(request)

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(partnershipRegOfficeAddressUrl)
          reset(mockActions)
        }

        "the form has no errors and no is selected" in {
          val userAnswers = UserAnswers(testUserId)
          setupDataRequiredActionBusinessPartners(userAnswers, mode)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody("uniqueTaxpayerReference_yesNo" -> "false")
            .withMethod("POST")
          val result = csrfAddToken(controller.next(index, mode))(request)

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(partnershipRegOfficeAddressUrl)
          reset(mockActions)
        }
      }
    }
  }
}
