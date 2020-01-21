/*
 * Copyright 2020 HM Revenue & Customs
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
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.connectors

class SignOutControllerSpec extends ControllerSpecWithGuiceApp {
  val mockExternalUrls = mock[connectors.ExternalUrls]
  val mockCommonDependencies = mock[CommonPlayDependencies]

  val controller = new SignOutController(mockCommonDependencies, mockExternalUrls, mockMcc)

  "SignOut Controller" should {
    "Redirect to logout" in {
      when(mockExternalUrls.ggLogoutUrl) thenReturn "ggLogout"
      when(mockExternalUrls.logoutContinueUrl) thenReturn "ggLogoutContinue"
      when(mockExternalUrls.ggOrigin) thenReturn "ggOrigin"

      val result = controller.signout().apply(FakeRequest())

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("ggLogout?continue=ggLogoutContinue&origin=ggOrigin")
    }

    "Redirect to the survey" in {
      when(mockExternalUrls.surveyRedirectUrl) thenReturn "surveyUrl"
      val result = controller.signedout().apply(FakeRequest())

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("surveyUrl")

    }
  }
}
