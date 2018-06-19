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

package uk.gov.hmrc.fhregistrationfrontend.actions

import org.mockito.ArgumentMatchers.{any, same}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress.EnrolmentProgress
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus.FhddsStatus

trait FhddsConnectorMocks extends MockitoSugar {
  this: ActionSpecBase ⇒

  val mockFhddsConnector = mock[FhddsConnector]

  def setupFhddsEnrolmentProgress(enrolmentProgress: EnrolmentProgress) = {
    when(mockFhddsConnector.getEnrolmentProgress(any())) thenReturn enrolmentProgress
  }

  def setupFhddsStatus(fhddsStatus: FhddsStatus) = {
    when(mockFhddsConnector.getStatus(same(registrationNumber))(any())) thenReturn fhddsStatus
  }
}
