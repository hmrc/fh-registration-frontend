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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerType
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnerTypePage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.RadioHelper

import scala.concurrent.Future

class BusinessPartnerControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]
  lazy val radioHelper = app.injector.instanceOf[RadioHelper]
  lazy val mockAppConfig = mock[FrontendAppConfig]
  val mockSessionCache = mock[SessionRepository]
  val index = 1

  def expectedRedirectLocationForPartnerType(partnerType: BusinessPartnerType.Value): String =
    partnerType match {
      case BusinessPartnerType.UnincorporatedBody =>
        routes.BusinessPartnersUnincorporatedBodyNameController.load(index, NormalMode).url
      case BusinessPartnerType.Partnership =>
        routes.BusinessPartnersPartnershipNameController.load(index, NormalMode).url
      case BusinessPartnerType.LimitedLiabilityPartnership =>
        routes.BusinessPartnersLtdLiabilityPartnershipNameController.load(index, NormalMode).url
      case BusinessPartnerType.CorporateBody =>
        routes.BusinessPartnersCorporateBodyCompanyNameController.load(index, NormalMode).url
      case _ =>
        routes.BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController.load(index, NormalMode).url
    }

  val controller =
    new BusinessPartnersController(commonDependencies, views, mockActions, mockSessionCache, mockMcc)(ec)

  List(NormalMode, CheckMode).foreach {
    case mode =>
      s"load in $mode" should {
        "Render the business partner page" when {
          "the useranswers contain page data" in {
            val userAnswers = UserAnswers(testUserId)
              .set(PartnerTypePage(index), BusinessPartnerType.CorporateBody)
              .success
              .value
            setupDataRetrievedAction(Some(userAnswers))

            val request = FakeRequest()
            val result = await(csrfAddToken(controller.load(index, mode))(request))

            status(result) shouldBe OK
            val page = Jsoup.parse(contentAsString(result))
            page.title should include("Business partners")
            reset(mockActions)
          }

          "the useranswers contain no page data" in {
            setupDataRetrievedAction(None)

            val request = FakeRequest()
            val result = await(csrfAddToken(controller.load(index, mode))(request))

            status(result) shouldBe OK
            val page = Jsoup.parse(contentAsString(result))
            page.title should include("Business partners")
            reset(mockActions)
          }
        }
      }

      s"next in $mode" when {
        BusinessPartnerType.partnerTypes.foreach { partnerType =>
          s"the user selects $partnerType" should {
            val expectedUrl = expectedRedirectLocationForPartnerType(partnerType)
            s"redirect to $expectedUrl" when {
              "the useranswers doesn't contain page data" in {
                setupDataRetrievedAction(None)
                when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

                val request = FakeRequest()
                  .withFormUrlEncodedBody("businessPartnersType" -> partnerType.toString)
                  .withMethod("POST")
                val result = await(csrfAddToken(controller.next(index, mode))(request))

                status(result) shouldBe SEE_OTHER
                redirectLocation(result) shouldBe Some(expectedUrl)
                reset(mockActions)
              }
              BusinessPartnerType.partnerTypes.filterNot(_ == partnerType).foreach { savedPartnerType =>
                s"the user answers contains partnerType $savedPartnerType" in {
                  val userAnswers = UserAnswers(testUserId)
                    .set(PartnerTypePage(index), savedPartnerType)
                    .success
                    .value
                  setupDataRetrievedAction(Some(userAnswers))
                  when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

                  val request = FakeRequest()
                    .withFormUrlEncodedBody("businessPartnersType" -> partnerType.toString)
                    .withMethod("POST")
                  val result = await(csrfAddToken(controller.next(index, mode))(request))

                  status(result) shouldBe SEE_OTHER
                  redirectLocation(result) shouldBe Some(expectedUrl)
                  reset(mockActions)
                }
              }
            }
            val noChangeExpectedUrl = if (mode == CheckMode) {
              routes.BusinessPartnersCheckYourAnswersController.load().url
            } else {
              expectedUrl
            }

            s"redirect to $noChangeExpectedUrl" when {
              s"the user answers contains partnerType $partnerType" in {
                val userAnswers = UserAnswers(testUserId)
                  .set(PartnerTypePage(index), partnerType)
                  .success
                  .value
                setupDataRetrievedAction(Some(userAnswers))

                val request = FakeRequest()
                  .withFormUrlEncodedBody("businessPartnersType" -> partnerType.toString)
                  .withMethod("POST")
                val result = await(csrfAddToken(controller.next(index, mode))(request))

                status(result) shouldBe SEE_OTHER
                redirectLocation(result) shouldBe Some(noChangeExpectedUrl)
                reset(mockActions)
              }
            }
          }
        }

        "return 400" when {
          "a radio button is not selected" in {
            setupDataRetrievedAction(None)

            val request = FakeRequest()
              .withFormUrlEncodedBody()
              .withMethod("POST")
            val result = await(csrfAddToken(controller.next(index, mode))(request))

            status(result) shouldBe BAD_REQUEST
            reset(mockActions)
          }
        }
      }
  }
}
