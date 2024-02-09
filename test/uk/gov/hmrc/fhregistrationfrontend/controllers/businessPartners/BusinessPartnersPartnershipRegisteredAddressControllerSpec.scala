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
import uk.gov.hmrc.fhregistrationfrontend.connectors.AddressLookupErrorResponse
import uk.gov.hmrc.fhregistrationfrontend.controllers.ControllerSpecWithGuiceApp
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, UkAddressLookup}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.UkAddressLookupPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.services.AddressService
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.http.BadRequestException

import scala.concurrent.Future

class BusinessPartnersPartnershipRegisteredAddressControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val mockAddressService: AddressService = mock[AddressService]
  val mockSessionCache: SessionRepository = mock[SessionRepository]
  val index = 1
  val controller =
    new BusinessPartnersPartnershipRegisteredAddressController(
      commonDependencies,
      views,
      mockActions,
      mockAppConfig,
      mockAddressService,
      mockSessionCache)(mockMcc)

  def createUserAnswers(answers: UkAddressLookup): UserAnswers =
    UserAnswers(testUserId)
      .set[UkAddressLookup](UkAddressLookupPage(1), answers)
      .success
      .value

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the business partner address page" when {
        "there are no user answers" in {
          setupDataRequiredActionBusinessPartners(emptyUserAnswers, mode)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("What is the partnership’s registered office address?")
          page.body.text() should include("What is Test User’s registered office address?")
          reset(mockActions)
        }

        "there are user answers" in {
          val userAnswers = createUserAnswers(UkAddressLookup(Some("44 test lane"), "SW1A 2AA"))
          setupDataRequiredActionBusinessPartners(userAnswers, mode)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("What is the partnership’s registered office address?")
          page.body.text() should include("What is Test User’s registered office address?")
          reset(mockActions)
        }
      }
    }

    s"next when in $mode" should {
      "redirect to the Choose Address page" when {
        "multiple addresses are found" in {
          setupDataRequiredActionBusinessPartners(emptyUserAnswers, mode)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAddressService.addressLookup(any(), any(), any())(any())).thenReturn(
            Future.successful(
              Right(
                Map(
                  "123" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, Some("123")),
                  "234" -> Address("45 test lane", None, None, None, "SW1A 2AA", None, Some("234")))
              )
            )
          )
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("partnerAddressLine", ""),
              ("partnerPostcode", "SW1A 2AA")
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(
            routes.BusinessPartnersChooseAddressController.load(index, mode).url)
          reset(mockActions)
        }
      }

      "redirect to the Confirm Address page" when {
        "a single address is found" in {
          setupDataRequiredActionBusinessPartners(emptyUserAnswers, mode)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAddressService.addressLookup(any(), any(), any())(any())).thenReturn(
            Future.successful(
              Right(
                Map(
                  "345" -> Address("1 Romford Road", None, None, None, "TF1 4ER", None, Some("345"))
                )
              )
            )
          )
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("partnerAddressLine", "1 Romford Road"),
              ("partnerPostcode", "TF1 4ER")
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(
            routes.BusinessPartnersPartnershipConfirmRegisteredAddressController.load(index, mode).url)
          reset(mockActions)
        }

        "the form data provided matches what is cached and" should {
          "not call address lookup" in {
            val userAnswers = createUserAnswers(
              UkAddressLookup(
                Some("44 test lane"),
                "SW1A 2AA",
                Map("1" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, None))))
            setupDataRequiredActionBusinessPartners(userAnswers, mode)
            when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
            when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

            val request = FakeRequest()
              .withFormUrlEncodedBody(("partnerPostcode", "SW1A 2AA"), ("partnerAddressLine", ""))
              .withMethod("POST")
            val result = await(csrfAddToken(controller.next(index, mode))(request))

            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get should include(
              routes.BusinessPartnersPartnershipConfirmRegisteredAddressController.load(index, mode).url)
            reset(mockActions)
          }
        }
      }

      "redirect to the Cannot Find Address page" when {
        "no addresses are found" in {
          val userAnswers = createUserAnswers(UkAddressLookup(Some("44 test lane"), "SW1A 2AA"))
          setupDataRequiredActionBusinessPartners(userAnswers, mode)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAddressService.addressLookup(any(), any(), any())(any())).thenReturn(
            Future.successful(Right(Map.empty))
          )

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("partnerPostcode", "HR33 7GP"),
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(
            routes.BusinessPartnersCannotFindAddressController.load(index, mode).url)
          reset(mockActions)
        }
      }

      "return 400" when {
        "the form has no errors, postcode is entered and address lookup returns an error" in {
          setupDataRequiredActionBusinessPartners(emptyUserAnswers, mode)

          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(
            mockAddressService
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

        "the postcode is missing in the form" in {
          setupDataRequiredActionBusinessPartners(emptyUserAnswers, mode)

          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(
            mockAddressService
              .addressLookup(any(), any(), any())(any()))
            .thenReturn(
              Future.successful(
                Left(AddressLookupErrorResponse(new BadRequestException("unknown")))
              ))

          val request = FakeRequest()
            .withFormUrlEncodedBody(("partnerAddressLine", "44"))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe BAD_REQUEST
        }

        "the postcode is an invalid format" in {
          setupDataRequiredActionBusinessPartners(emptyUserAnswers, mode)

          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(
            mockAddressService
              .addressLookup(any(), any(), any())(any()))
            .thenReturn(
              Future.successful(
                Left(AddressLookupErrorResponse(new BadRequestException("unknown")))
              ))

          val request = FakeRequest()
            .withFormUrlEncodedBody(("partnerPostcode", "invalid postcode"), ("partnerAddressLine", "44"))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe BAD_REQUEST
        }

        "address line is invalid (contains too many characters)" in {
          setupDataRequiredActionBusinessPartners(emptyUserAnswers, mode)

          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(
            mockAddressService
              .addressLookup(any(), any(), any())(any()))
            .thenReturn(
              Future.successful(
                Left(AddressLookupErrorResponse(new BadRequestException("unknown")))
              ))

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("partnerPostcode", "SW1A 2AA"),
              ("partnerAddressLine", "this Address Line is too long this Address Line is too long")
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe BAD_REQUEST
        }
      }
    }
  }
}
