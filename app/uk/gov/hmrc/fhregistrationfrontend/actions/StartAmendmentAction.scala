/*
 * Copyright 2019 HM Revenue & Customs
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

import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus.{FhddsStatus, Processing, Received}
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService

import scala.concurrent.ExecutionContext

class StartAmendmentAction(fhddsConnector: FhddsConnector) (implicit save4LaterService: Save4LaterService, errorHandler: ErrorHandler, val executionContext: ExecutionContext)
    extends StartUpdateAction(fhddsConnector)
{

  override def isAllowed(fhddsStatus: FhddsStatus): Boolean = fhddsStatus match {
    case Received | Processing ⇒ true
    case _                     ⇒ false
  }
}
