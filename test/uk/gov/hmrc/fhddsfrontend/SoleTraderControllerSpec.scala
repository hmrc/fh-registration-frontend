/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.fhddsfrontend

import play.api.http.Status
import uk.gov.hmrc.fhddsfrontend.controllers.SoleTraderController
import uk.gov.hmrc.play.frontend.auth.connectors.AuthConnector


class SoleTraderControllerSpec extends AppUnitGenerator {

  object soleTraderController extends SoleTraderController(ds) {
    implicit lazy val mockAuthConnector: AuthConnector = mock[AuthConnector]
    override val authConnector: AuthConnector = mockAuthConnector
  }

  "SoleTraderController" should {
    "information return 303" in {
      val result = csrfAddToken(soleTraderController.information())(request)
      status(result) shouldBe Status.SEE_OTHER
    }

    "submitCheckResult return 303" in {
      val result = csrfAddToken(soleTraderController.submitCheckResult())(request)
      status(result) shouldBe Status.SEE_OTHER
    }
  }
}
