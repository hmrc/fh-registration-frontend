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
import org.jsoup.Jsoup
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.EmailVerificationForm
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, EmailVerificationConnectorMocks, InMemoryShortLivedCache}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.views.Views

class ContactPersonControllerSpec
    extends ControllerSpecWithGuiceApp with EmailVerificationConnectorMocks with ActionsMock with BeforeAndAfterEach {

  SharedMetricRegistries.clear()

  override lazy val views = app.injector.instanceOf[Views]
  lazy val mockAppConfig = mock[FrontendAppConfig]

  val controller = new ContactPersonController(commonDependencies, views, mockActions, mockAppConfig)(mockMcc)

  "load" should {
    "Render the Contact Person page" when {
      "The business partner v2 pages are enabled" in {
        setupUserAction()

        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe OK
        val page = Jsoup.parse(contentAsString(result))
        page.title() should include("Contact person's details")
        reset(mockActions)
      }
    }

    "render the not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.load())(request))

        status(result) shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title should include("Page not found")
        reset(mockActions)
      }
    }
  }

  "next" when {
    "The business partner v2 pages are enabled" should {
      "return 200" when {
        "details are entered and Yes radio button selected" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "firstName"               -> "John",
              "lastName"                -> "Smith",
              "jobTitle"                -> "Astronaut",
              "telephone"               -> "0123456789",
              "usingSameContactAddress" -> "true"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe OK
          contentAsString(result) shouldBe "Form submitted, with result: ContactPerson(John,Smith,Astronaut,0123456789,None,true,None,None,None)"
          reset(mockActions)
        }
      }

      "return 200" when {
        "details are entered," +
          "Is this the address you want to use? No" +
          "Is the contact address in the UK? Yes" +
          "Only mandatory fields entered" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "firstName"                                     -> "John",
              "lastName"                                      -> "Smith",
              "jobTitle"                                      -> "Astronaut",
              "telephone"                                     -> "0123456789",
              "usingSameContactAddress"                       -> "false",
              "isUkAddress"                                   -> "true",
              "otherUkContactAddress_contactAddress.Line1"    -> "Flat 1",
              "otherUkContactAddress_contactAddress.postcode" -> "AB1 2YZ"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe OK
          contentAsString(result) shouldBe "Form submitted, with result: ContactPerson(John,Smith,Astronaut,0123456789,None,false,Some(true),Some(Address(Flat 1,None,None,None,AB1 2YZ,None,None)),None)"
          reset(mockActions)
        }
      }

      "return 200" when {
        "details are entered," +
          "Is this the address you want to use? No" +
          "Is the contact address in the UK? Yes" +
          "All fields entered" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "firstName"                                     -> "John",
              "lastName"                                      -> "Smith",
              "jobTitle"                                      -> "Astronaut",
              "telephone"                                     -> "0123456789",
              "usingSameContactAddress"                       -> "false",
              "isUkAddress"                                   -> "true",
              "otherUkContactAddress_contactAddress.Line1"    -> "Flat 1",
              "otherUkContactAddress_contactAddress.Line2"    -> "5 High Street",
              "otherUkContactAddress_contactAddress.Line3"    -> "Worthing",
              "otherUkContactAddress_contactAddress.Line4"    -> "West Sussex",
              "otherUkContactAddress_contactAddress.postcode" -> "AB1 2YZ"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe OK
          contentAsString(result) shouldBe "Form submitted, with result: ContactPerson(John,Smith,Astronaut,0123456789,None,false,Some(true),Some(Address(Flat 1,Some(5 High Street),Some(Worthing),Some(West Sussex),AB1 2YZ,None,None)),None)"
          reset(mockActions)
        }
      }

      "return 200" when {
        "details are entered," +
          "Is this the address you want to use? No" +
          "Is the contact address in the UK? No" +
          "Only mandatory fields entered" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "firstName"                                                   -> "John",
              "lastName"                                                    -> "Smith",
              "jobTitle"                                                    -> "Astronaut",
              "telephone"                                                   -> "0123456789",
              "usingSameContactAddress"                                     -> "false",
              "isUkAddress"                                                 -> "false",
              "otherInternationalContactAddress_contactAddress.Line1"       -> "Flat 1",
              "otherInternationalContactAddress_contactAddress.countryCode" -> "FR"
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe OK
          contentAsString(result) shouldBe "Form submitted, with result: ContactPerson(John,Smith,Astronaut,0123456789,None,false,Some(false),None,Some(InternationalAddress(Flat 1,None,None,line4,FR)))"
          reset(mockActions)
        }
      }

      "return 400" when {
        "no data is entered" in {
          setupUserAction()

          when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)
          val request = FakeRequest()
            .withFormUrlEncodedBody(
              "firstName" -> "",
              "lastName"  -> "",
              "jobTitle"  -> "",
              "telephone" -> "",
            )
            .withMethod("POST")
          val result = await(csrfAddToken(controller.next())(request))

          status(result) shouldBe BAD_REQUEST
          val page = Jsoup.parse(contentAsString(result))
          page.title should include("Contact person's details")
          reset(mockActions)
        }
      }
    }

    "the new business partner pages are disabled" should {
      "render the not found page" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
        val result = await(csrfAddToken(controller.next())(request))

        status(result) shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title should include("Page not found")
        reset(mockActions)
      }
    }
  }
}
