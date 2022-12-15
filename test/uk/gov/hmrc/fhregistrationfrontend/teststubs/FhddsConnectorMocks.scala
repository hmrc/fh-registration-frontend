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

import java.time.LocalDate
import java.util.Date

import org.mockito.ArgumentMatchers.{any, same}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.models.des.SubscriptionDisplayWrapper
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress.EnrolmentProgress
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus.FhddsStatus
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

import scala.concurrent.Future

trait FhddsConnectorMocks extends MockitoSugar with UserTestData {
  this: UnitSpec =>

  val mockFhddsConnector = mock[FhddsConnector]

  def setupFhddsEnrolmentProgress(enrolmentProgress: EnrolmentProgress) =
    when(mockFhddsConnector.getEnrolmentProgress(any())) thenReturn enrolmentProgress

  def setupFhddsStatus(fhddsStatus: FhddsStatus, registrationNumber: String = registrationNumber) =
    when(mockFhddsConnector.getStatus(same(registrationNumber))(any())) thenReturn fhddsStatus

  def setupDesDisplayResult(
    jsonFile: String = "limited-company/fhdds-limited-company-large-uk",
    registrationNumber: String = registrationNumber) =
    when(mockFhddsConnector.getSubmission(same(registrationNumber))(any())) thenReturn Future.successful(
      loadDesDataFile(jsonFile))

  private def loadDesDataFile(jsonFile: String): SubscriptionDisplayWrapper = {
    val resource = getClass.getResourceAsStream(s"/json/valid/display/$jsonFile.json")
    Json.parse(resource).as[SubscriptionDisplayWrapper]
  }

  def setupDeregister(registrationNumber: String = registrationNumber, date: LocalDate = LocalDate.now()) =
    when(mockFhddsConnector.deregister(same(registrationNumber), any())(any())) thenReturn Future.successful(new Date())

  def setupWithdrawal(registrationNumber: String = registrationNumber, date: LocalDate = LocalDate.now()) =
    when(mockFhddsConnector.withdraw(same(registrationNumber), any())(any())) thenReturn Future.successful(new Date())

}
