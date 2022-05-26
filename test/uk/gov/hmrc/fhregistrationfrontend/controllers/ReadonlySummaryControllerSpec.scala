/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.test.FakeRequest
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus
import uk.gov.hmrc.fhregistrationfrontend.services.mapping.DesToFormImpl
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, FhddsConnectorMocks, Save4LaterMocks}
import uk.gov.hmrc.fhregistrationfrontend.views.Mode

class ReadonlySummaryControllerSpec
    extends ControllerSpecWithGuiceApp with FhddsConnectorMocks with ActionsMock with Save4LaterMocks {

  val controller = new ReadOnlySummaryController(
    commonDependencies,
    new DesToFormImpl(),
    mockFhddsConnector,
    mockMcc,
    mockActions,
    views
  )(scala.concurrent.ExecutionContext.Implicits.global)

  "view" should {
    "Render the summary for an approved application" in {
      setupEnrolledUserAction()
      setupDesDisplayResult()
      setupFhddsStatus(FhddsStatus.Approved)

      val request = FakeRequest()
      val result = await(controller.view()(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages(s"fh.summary.${Mode.ReadOnlyApplication}.title"))
    }

    "Render the summary for a rejected application" in {
      setupEnrolledUserAction()
      setupDesDisplayResult()
      setupFhddsStatus(FhddsStatus.Revoked)

      val request = FakeRequest()
      val result = await(controller.view()(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages(s"fh.summary.${Mode.ReadOnlyRegister}.title"))
    }
  }
}
