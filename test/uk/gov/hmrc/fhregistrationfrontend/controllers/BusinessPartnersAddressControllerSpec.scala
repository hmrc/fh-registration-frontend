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
import uk.gov.hmrc.fhregistrationfrontend.connectors.AddressLookupErrorResponse
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, UkAddressLookup}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.BusinessPartnerAddressPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.services.AddressService
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}

import scala.concurrent.Future

class BusinessPartnersAddressControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  implicit val hc = HeaderCarrier()

  override lazy val views = app.injector.instanceOf[Views]

  val mockAppConfig = mock[FrontendAppConfig]

  val mockAddressService = mock[AddressService]
  val index = 1
  val mockSessionCache = mock[SessionRepository]
  val controller =
    new BusinessPartnersAddressController(
      commonDependencies,
      views,
      mockActions,
      mockAppConfig,
      mockAddressService,
      mockSessionCache)(mockMcc)

  List(NormalMode, CheckMode).foreach { mode =>
    s"next when in $mode" should {
      "load" should {
        "Render the business partner address page" when {
          "the new business partner pages are enabled and there is page data" in {
            //todo add check for page answers populated
            val addressLookup = UkAddressLookup(Some("addressLime"), "wn75lg")
            val userAnswers = UserAnswers(testUserId)
              .set(BusinessPartnerAddressPage(index), addressLookup).success.value
            setupDataRequiredAction(userAnswers)
            userAnswers
              .set[UkAddressLookup](BusinessPartnerAddressPage(1), addressLookup)
              .success
              .value
            when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
            val request = FakeRequest()
            val result = await(csrfAddToken(controller.load(index, mode))(request))

            status(result) shouldBe OK
            val page = Jsoup.parse(contentAsString(result))
            page.title should include("What is the partner’s address?")
            reset(mockActions)
          }
          "the new business partner pages are enabled and there is no page data" in {
            val userAnswers = UserAnswers(testUserId)
            setupDataRequiredAction(userAnswers)
            val addressLookup = UkAddressLookup(Some("addressLime"), "wn75lg")
            userAnswers
              .set[UkAddressLookup](BusinessPartnerAddressPage(1), addressLookup)
              .success
              .value
            when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
            val request = FakeRequest()
            val result = await(csrfAddToken(controller.load(index, mode))(request))

            status(result) shouldBe OK
            val page = Jsoup.parse(contentAsString(result))
            page.title should include("What is the partner’s address?")
            reset(mockActions)
          }
        }
      }
    }
    s"next when in $mode" should {
      "next" when {
        "the new business partner pages are enabled" should {
          "redirect to choose address" when {
            "the form has no errors, postcode is entered and address found and there is page data" in {
              val addressLookup = UkAddressLookup(Some("addressLime"), "wn75lg")
              val userAnswers = UserAnswers(testUserId)
                .set(BusinessPartnerAddressPage(index), addressLookup).success.value
              setupDataRequiredAction(userAnswers)
              userAnswers
                .set[UkAddressLookup](BusinessPartnerAddressPage(1), addressLookup)
                .success
                .value
              when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
              when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
              when(mockAddressService
                .addressLookup(any(), any(), any())(any()))
                .thenReturn(
                  Future.successful(
                    Right(
                      Map(
                        "123" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, Some("123")),
                        "234" -> Address("77 test lane", None, None, None, "SW1A 2AA", None, Some("234")))
                    )
                  ))
              val request = FakeRequest()
                .withFormUrlEncodedBody(("partnerPostcode", "SW1A 2AA"), ("partnerAddressLine", ""))
                .withMethod("POST")
              val result = await(csrfAddToken(controller.next(index, mode))(request))

              status(result) shouldBe SEE_OTHER
              redirectLocation(result).get should include("/business-partners/choose-address")
              reset(mockActions)
            }
          }

          "return 400" when {
            "the form has no errors, postcode is entered and address lookup returns an error" in {
              setupUserAction()
              when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
              when(mockAddressService
                .addressLookup(any(), any(), any())(any()))
                .thenReturn(
                  Future.successful(
                    Left(AddressLookupErrorResponse(new BadRequestException("unknown")))
                  ))
              val request = FakeRequest()
                .withFormUrlEncodedBody(("partnerPostcode", "SW1A 2AA"), ("partnerAddressLine", "44"))
                .withMethod("POST")
              val result = await(csrfAddToken(controller.next(index, mode))(request))

              status(result) shouldBe BAD_REQUEST
            }

            "the postcode is missing in form" in {
              setupUserAction()
              when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
              val request = FakeRequest()
                .withFormUrlEncodedBody(("partnerAddressLine", "44"))
                .withMethod("POST")
              val result = await(csrfAddToken(controller.next(index, mode))(request))

              status(result) shouldBe BAD_REQUEST
            }

            "the postcode is an invalid format" in {
              setupUserAction()
              when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
              val request = FakeRequest()
                .withFormUrlEncodedBody(("partnerPostcode", "invalid"), ("partnerAddressLine", "44"))
                .withMethod("POST")
              val result = await(csrfAddToken(controller.next(index, mode))(request))

              status(result) shouldBe BAD_REQUEST
            }
          }
        }

        "Render the Not found page" when {
          "the new business partner pages are disabled" in {
            setupUserAction()
            when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
            val request = FakeRequest()
              .withFormUrlEncodedBody(("partnerPostcode", "SW1A 2AA"))
              .withMethod("POST")
            val result = await(csrfAddToken(controller.next(index, mode))(request))

            status(result) shouldBe NOT_FOUND
            val page = Jsoup.parse(contentAsString(result))
            page.title() should include("Page not found")
            reset(mockActions)
          }
        }
      }
    }
  }
}
