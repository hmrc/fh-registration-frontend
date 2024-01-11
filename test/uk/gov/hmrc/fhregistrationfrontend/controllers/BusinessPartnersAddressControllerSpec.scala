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
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.connectors.AddressLookupErrorResponse
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, UkAddressLookup}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.UkAddressLookupPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.services.AddressService
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.mvc.Cookie

import scala.concurrent.Future

class BusinessPartnersAddressControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override lazy val views: Views = app.injector.instanceOf[Views]

  val mockAppConfig = mock[FrontendAppConfig]
  val mockAddressService: AddressService = mock[AddressService]
  lazy val mockSessionCache: SessionRepository = mock[SessionRepository]
  val index = 1

  val controller = new BusinessPartnersAddressController(
    commonDependencies,
    views,
    mockActions,
    mockAppConfig,
    mockAddressService,
    mockSessionCache
  )(mockMcc)

  def createUserAnswers(answers: UkAddressLookup): UserAnswers =
    UserAnswers(testUserId)
      .set[UkAddressLookup](UkAddressLookupPage(1), answers)
      .success
      .value
  val emptyUserAnswers: UserAnswers = UserAnswers(testUserId)

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the business partner address page" when {
        "The business partner v2 pages are enabled and there is no page data" in {
          setupDataRequiredAction(emptyUserAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("What is the partner’s address?")
          reset(mockActions)
        }

        "The business partner v2 pages are enabled and there are userAnswers with page data" in {
          val userAnswers = createUserAnswers(UkAddressLookup(Some("44 test lane"), "SW1A 2AA"))
          setupDataRequiredAction(userAnswers)
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

    s"next when in $mode" should {
      "redirect to choose address page" when {
        "the form has no errors and multiple addresses are returned from UkAddressLookup" in {
          setupDataRequiredAction(emptyUserAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockAddressService.addressLookup(any(), any(), any())(any())).thenReturn(
            Future.successful(
              Right(
                Map(
                  "1" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, None),
                  "2" -> Address("77 test lane", None, None, None, "SW1A 2AA", None, None))
              )
            )
          )
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody(("partnerPostcode", "SW1A 2AA"), ("partnerAddressLine", ""))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(routes.BusinessPartnersChooseAddressController.load().url.drop(6))
          reset(mockActions)
        }

      }

      "redirect to confirm address page" when {
        "the form has no errors and a single address is returned from UkAddressLookup" in {
          setupDataRequiredAction(emptyUserAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
          when(mockAddressService.addressLookup(any(), any(), any())(any())).thenReturn(
            Future.successful(
              Right(
                Map(
                  "1" -> Address("44 test lane", None, None, None, "SW1A 2AA", None, None)
                )
              ))
          )

          val request = FakeRequest()
            .withFormUrlEncodedBody(("partnerPostcode", "SW1A 2AA"), ("partnerAddressLine", "44 test lane"))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(
            routes.BusinessPartnersConfirmAddressController.load(index, mode).url.drop(6))
          reset(mockActions)
        }
      }

      "redirect to cannot find address page" when {
        "the form has no errors and an empty address list is returned from UkAddressLookup" in {
          setupDataRequiredAction(emptyUserAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))
          when(mockAddressService.addressLookup(any(), any(), any())(any())).thenReturn(
            Future.successful(Right(Map.empty))
          )

          val request = FakeRequest()
            .withFormUrlEncodedBody(("partnerPostcode", "SW1A 2AA"), ("partnerAddressLine", "44 test lane"))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe SEE_OTHER
          redirectLocation(result).get should include(
            routes.BusinessPartnersCannotFindAddressController.load(index, mode).url.drop(6))
          reset(mockActions)
        }

        "the form has no errors and postcode matches what is saved in Cache" should {
          "Not call Address Lookup" in {
            val userAnswers = createUserAnswers(UkAddressLookup(Some("44 test lane"), "SW1A 2AA"))
            setupDataRequiredAction(userAnswers)
            when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
            when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

            val request = FakeRequest()
              .withFormUrlEncodedBody(("partnerPostcode", "SW1A 2AA"), ("partnerAddressLine", ""))
              .withMethod("POST")
            val result = await(csrfAddToken(controller.next(index, mode))(request))

            status(result) shouldBe SEE_OTHER
            redirectLocation(result).get should include(
              routes.BusinessPartnersCannotFindAddressController.load(index, mode).url.drop(6))
            reset(mockActions)
          }
        }
      }

      "return 400" when {
        "the form has no errors, postcode is entered and address lookup returns an error" in {
          setupDataRequiredAction(emptyUserAnswers)

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

        "the postcode is missing in form" in {
          setupDataRequiredAction(emptyUserAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          when(mockSessionCache.set(any())).thenReturn(Future.successful(true))

          val request = FakeRequest()
            .withFormUrlEncodedBody(("partnerAddressLine", "44"))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe BAD_REQUEST
        }

        "the postcode is an invalid format" in {
          setupDataRequiredAction(emptyUserAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)

          val request = FakeRequest()
            .withFormUrlEncodedBody(("partnerPostcode", "invalid"), ("partnerAddressLine", "44"))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe BAD_REQUEST
        }

        "address line is invalid (contains too many characters)" in {
          setupDataRequiredAction(emptyUserAnswers)
          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)

          val request = FakeRequest()
            .withFormUrlEncodedBody(
              ("partnerPostcode", "SW1A 2AA"),
              ("partnerAddressLine", "this Address Line is too long this Address Line is too long"))
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next(index, mode))(request))

          status(result) shouldBe BAD_REQUEST
        }
      }

    }
  }
}
