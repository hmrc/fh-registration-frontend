/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.views

import org.jsoup.Jsoup
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.config.{ErrorHandler, FrontendAppConfig}
import uk.gov.hmrc.fhregistrationfrontend.controllers.{BusinessCustomersController, ControllerSpecWithGuiceApp}
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, EmailVerificationConnectorMocks}
import uk.gov.hmrc.fhregistrationfrontend.views.html.error_template

class ErrorTemplateViewSpec
    extends ViewSpecHelper with ControllerSpecWithGuiceApp with EmailVerificationConnectorMocks with ActionsMock
    with BeforeAndAfterEach {
  val error_template_view: error_template = views.error_template
  val errorHandler: ErrorHandler = app.injector.instanceOf[ErrorHandler]
  lazy val mockAppConfig = mock[FrontendAppConfig]
  val controller = new BusinessCustomersController(commonDependencies, views, mockActions, mockAppConfig)(mockMcc)
  object Selectors {
    val body = "govuk-body"
    val heading = "govuk-heading-l"
    val button = "govuk-button"
  }

  "error_template for page not found" should {

    "render the page not found page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest()
        val result = csrfAddToken(controller.load())(request)

        status(result) shouldBe NOT_FOUND
        val page = Jsoup.parse(contentAsString(result))
        page.title mustEqual "Page not found - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK"
        page.getElementsByTag("h1").text mustEqual "This page can’t be found"
        page
          .getElementsByClass(Selectors.body)
          .text() mustBe "Please check that you have entered the correct web address."
        reset(mockActions)
      }
    }
  }

  "error_template for technical issues" should {
    "render the technical issues page" when {
      "the new business partner pages are disabled" in {
        setupUserAction()
        when(mockAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
        val request = FakeRequest("POST", "/")
        val result = csrfAddToken(controller.load())(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        val page = Jsoup.parse(contentAsString(result))
        page.title mustEqual "Internal Server Error - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK"
        page.getElementsByTag("h1").text mustEqual "Sorry, we’re experiencing technical difficulties"
        page
          .getElementsByClass(Selectors.body)
          .text() mustBe "Please try again in a few minutes."
        reset(mockActions)
      }
    }
  }

}
