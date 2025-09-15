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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.services.SummaryConfirmationService
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock

import scala.concurrent.Future

class PdfDownloadControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock with BeforeAndAfterEach {

  val mockKeyStore = mock[SummaryConfirmationService]

  val controller = new PdfDownloadController(commonDependencies, mockKeyStore, mockMcc, mockActions)(
    using scala.concurrent.ExecutionContext.Implicits.global
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockKeyStore)
  }

  "downloadPrintable" should {
    "Return an error when summary is not found in the keystore" in {
      setupUserAction()
      setupKeyStore(None)
      val request = FakeRequest()

      val result = csrfAddToken(controller.downloadPrintable())(request)

      status(result) shouldBe BAD_REQUEST
    }

    "Return the summary after removing script tags" in {
      val summary = "<html><body><script>alert()</script>summary</body></html>"
      val expectedSummary = "<html><body>summary</body></html>"
      val request = FakeRequest()
      setupUserAction()
      setupKeyStore(Some(summary))

      val result = csrfAddToken(controller.downloadPrintable())(request)

      status(result) shouldBe OK
      contentAsString(result) shouldBe expectedSummary

    }
  }

  def setupKeyStore(summaryText: Option[String]): Unit =
    when(mockKeyStore.fetchSummaryForPrint()(using any())) `thenReturn` Future.successful(summaryText)
}
