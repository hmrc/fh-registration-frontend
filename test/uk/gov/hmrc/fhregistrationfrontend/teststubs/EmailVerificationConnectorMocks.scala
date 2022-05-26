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

package uk.gov.hmrc.fhregistrationfrontend.teststubs

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.fhregistrationfrontend.connectors.EmailVerificationConnector
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

trait EmailVerificationConnectorMocks extends MockitoSugar {
  this: UnitSpec ⇒

  val mockEmailVerifcationConnector = mock[EmailVerificationConnector]

  def setupEmailVerificationConnector(email: String, isVerified: Boolean) = {
    when(mockEmailVerifcationConnector.isVerified(ArgumentMatchers.eq(email))(any())) thenReturn isVerified
    when(mockEmailVerifcationConnector.requestVerification(ArgumentMatchers.eq(email), any())(any())) thenReturn isVerified
  }

}
