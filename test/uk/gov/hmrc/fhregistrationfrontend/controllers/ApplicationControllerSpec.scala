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
import play.api.mvc.Results
import uk.gov.hmrc.auth.core.Enrolments
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.fhregistrationfrontend.AppUnitGenerator

import scala.concurrent.Future


class ApplicationControllerSpec extends AppUnitGenerator with Results {

  type RType = Option[String] ~ Option[String] ~ Enrolments
  val authResult = new ~ (new ~(Option("id"), Option("email@test.com")), new Enrolments(Set.empty))

  when(mockAuthConnector.authorise(any(), any[Retrieval[RType]]())(any(),any()))
    .thenReturn(Future.successful(authResult))

  when(mockConfiguration.getInt(s"formMaxExpiryDays")).thenReturn(Some(27))


  val applicationController =
    new Application(new ExternalUrls(ds), ds, mockFhddsConnector, mockBusinessCustomerFrontendConnector, mockActions)(mockSave4Later) {

    override val authConnector = mockAuthConnector
  }

  "errorResultsPages" should {
    "show related error page" in {
      val resultNotFound = ds.errorHandler.errorResultsPages(Results.NotFound)
      resultNotFound.value.get.toString shouldBe "Success(Result(404, Map()))"

      val resultBadRequest = ds.errorHandler.errorResultsPages(Results.BadRequest)
      resultBadRequest.value.get.toString shouldBe "Success(Result(400, Map()))"

      val resultServiceUnavailable = ds.errorHandler.errorResultsPages(Results.ServiceUnavailable)
      resultServiceUnavailable.value.get.toString shouldBe "Success(Result(500, Map()))"
    }

  }

}
