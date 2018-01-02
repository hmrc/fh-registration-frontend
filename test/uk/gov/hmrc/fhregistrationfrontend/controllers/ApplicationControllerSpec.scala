/*
 * Copyright 2018 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Configuration
import play.api.http.Status
import play.api.i18n.MessagesApi
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.fhregistrationfrontend.AppUnitGenerator

import scala.concurrent.Future


class ApplicationControllerSpec extends AppUnitGenerator {

  val applicationController = new Application(new ExternalUrls(ds), ds, mockFhddsConnector, mock[MessagesApi], mock[Configuration]) {
    override val authConnector = mockAuthConnector
    override val usewhiteListing = false
  }

  "GET /" should {

    when(mockAuthConnector.authorise(any(), any[Retrieval[Unit]]())(any(),any()))
      .thenReturn(Future.successful(()))

    val expectedRedirect = "http://localhost:9923/business-customer/FHDDS?backLinkUrl=http://localhost:1118/fhdds/continue"

    "return 303" in {
      val result = applicationController.start().apply(request)
      result.header.status shouldBe Status.SEE_OTHER
      redirectLocation(result) shouldBe Some(expectedRedirect)
    }

  }

}
