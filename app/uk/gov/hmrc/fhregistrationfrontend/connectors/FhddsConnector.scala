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

package uk.gov.hmrc.fhregistrationfrontend.connectors

import java.util.Date
import javax.inject.{Inject, Singleton}

import play.api.libs.json.Reads
import play.api.{Configuration, Environment}
import uk.gov.hmrc.fhregistration.models.fhdds.{SubmissionRequest, SubmissionResponse}
import uk.gov.hmrc.fhregistrationfrontend.models.des.{SubscriptionDisplayWrapper, WithdrawalRequest}
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus.FhddsStatus
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._

import scala.concurrent.Future



@Singleton
class FhddsConnector @Inject() (
  val http: HttpClient,
  override val runModeConfiguration: Configuration,
  environment: Environment
) extends ServicesConfig
{

  override protected def mode = environment.mode
  val FHDSSServiceUrl: String = baseUrl("fhdds")

  def getStatus(fhddsRegistrationNumber: String)(implicit headerCarrier: HeaderCarrier): Future[FhddsStatus] = {
    http.GET[FhddsStatus](s"$FHDSSServiceUrl/fhdds/subscription/$fhddsRegistrationNumber/status")
  }

  def getSubmission(fhddsRegistrationNumber: String)(implicit headerCarrier: HeaderCarrier): Future[SubscriptionDisplayWrapper] = {
    http.GET[SubscriptionDisplayWrapper](s"$FHDSSServiceUrl/fhdds/subscription/$fhddsRegistrationNumber/get")
  }

  def createSubmission(safeId: String, currentRegNumber: Option[String], request: SubmissionRequest)(implicit headerCarrier: HeaderCarrier): Future[SubmissionResponse] = {
    val url = currentRegNumber.fold(
      s"$FHDSSServiceUrl/fhdds/subscription/subscribe/$safeId"
    ) { currentRegNumber ⇒
      s"$FHDSSServiceUrl/fhdds/subscription/subscribe/$safeId?currentRegNumber=$currentRegNumber"
    }

    http.POST[SubmissionRequest, SubmissionResponse](url, request)
  }

  def amendSubmission(fhddsRegistrationNumber: String, request: SubmissionRequest)(implicit headerCarrier: HeaderCarrier): Future[SubmissionResponse] = {
    http.POST[SubmissionRequest, SubmissionResponse](s"$FHDSSServiceUrl/fhdds/subscription/amend/$fhddsRegistrationNumber", request)
  }

  def withdraw(fhddsRegistrationNumber: String, request: WithdrawalRequest)(implicit headerCarrier: HeaderCarrier): Future[Date] = {
    http.POST[WithdrawalRequest, Date](s"$FHDSSServiceUrl/fhdds/subscription/withdrawal/$fhddsRegistrationNumber", request)
  }
  def getEnrolmentProgress(implicit hc: HeaderCarrier): Future[EnrolmentProgress.EnrolmentProgress] = {
    implicit val reads = Reads.enumNameReads(EnrolmentProgress)
    http.GET[EnrolmentProgress.EnrolmentProgress](s"$FHDSSServiceUrl/fhdds/subscription/enrolmentProgress")
  }

}