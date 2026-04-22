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

package uk.gov.hmrc.fhregistrationfrontend.connectors

import java.util.Date
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{Json, Reads}
import play.api.Configuration
import uk.gov.hmrc.fhregistration.models.fhdds.{SubmissionRequest, SubmissionResponse}
import uk.gov.hmrc.fhregistrationfrontend.models.des.{DeregistrationRequest, SubscriptionDisplayWrapper, WithdrawalRequest}
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus.FhddsStatus
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.SubmissionOutcome
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.SubmissionOutcome.ActiveSubscription
import uk.gov.hmrc.fhregistrationfrontend.models.submissiontracking.SubmissionTracking
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.client.HttpClientV2
import play.api.libs.ws.writeableOf_JsValue

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object FhddsConnector {
  final case class SubmissionErrorResponse(code: String, reason: String)

  object SubmissionErrorResponse {
    implicit val reads: Reads[SubmissionErrorResponse] = Json.reads[SubmissionErrorResponse]
  }
}

@Singleton
class FhddsConnector @Inject() (
  val http: HttpClientV2,
  val runModeConfiguration: Configuration
)(implicit ec: ExecutionContext)
    extends ServicesConfig(runModeConfiguration) {
  import FhddsConnector._

  private val activeSubscriptionCode = "ACTIVE_SUBSCRIPTION"
  val FHDSSServiceUrl: String = baseUrl("fhdds")

  private def submissionErrorResponse(response: HttpResponse): SubmissionErrorResponse =
    Try(response.json.as[SubmissionErrorResponse])
      .getOrElse(SubmissionErrorResponse("UNKNOWN_SUBMISSION_ERROR", response.body))

  private def submissionRead(response: HttpResponse): Either[SubmissionOutcome, SubmissionResponse] =
    response.status match {
      case status if status >= 200 && status < 300 =>
        Right(response.json.as[SubmissionResponse])
      case status if status >= 400 && status < 500 =>
        val error = submissionErrorResponse(response)
        if (error.code == activeSubscriptionCode) Left(ActiveSubscription)
        else throw UpstreamErrorResponse(s"$status received from fhdds: ${error.reason}", status, status)
      case status =>
        throw UpstreamErrorResponse(s"$status received from fhdds", status, status)
    }

  def getStatus(fhddsRegistrationNumber: String)(implicit headerCarrier: HeaderCarrier): Future[FhddsStatus] = {
    val url = s"$FHDSSServiceUrl/fhdds/subscription/$fhddsRegistrationNumber/status"
    http.get(url"$url").execute[FhddsStatus]
  }

  def getSubmission(fhddsRegistrationNumber: String)(implicit
    headerCarrier: HeaderCarrier
  ): Future[SubscriptionDisplayWrapper] = {
    val url = s"$FHDSSServiceUrl/fhdds/subscription/$fhddsRegistrationNumber/get"
    http.get(url"$url").execute[SubscriptionDisplayWrapper]
  }

  def createSubmission(safeId: String, currentRegNumber: Option[String], request: SubmissionRequest)(implicit
    headerCarrier: HeaderCarrier
  ): Future[Either[SubmissionOutcome, SubmissionResponse]] = {
    val url = currentRegNumber.fold(
      s"$FHDSSServiceUrl/fhdds/subscription/subscribe/$safeId"
    ) { currentRegNumber =>
      s"$FHDSSServiceUrl/fhdds/subscription/subscribe/$safeId?currentRegNumber=$currentRegNumber"
    }

    http.post(url"$url").withBody(Json.toJson(request)).execute[HttpResponse].map(submissionRead)
  }

  def amendSubmission(fhddsRegistrationNumber: String, request: SubmissionRequest)(implicit
    headerCarrier: HeaderCarrier
  ): Future[Either[SubmissionOutcome, SubmissionResponse]] = {
    val url = s"$FHDSSServiceUrl/fhdds/subscription/amend/$fhddsRegistrationNumber"
    http.post(url"$url").withBody(Json.toJson(request)).execute[HttpResponse].map(submissionRead)
  }

  def withdraw(fhddsRegistrationNumber: String, request: WithdrawalRequest)(implicit
    headerCarrier: HeaderCarrier
  ): Future[Date] = {
    val url = s"$FHDSSServiceUrl/fhdds/subscription/withdrawal/$fhddsRegistrationNumber"
    http.post(url"$url").withBody(Json.toJson(request)).execute[Date]
  }

  def deregister(fhddsRegistrationNumber: String, request: DeregistrationRequest)(implicit
    headerCarrier: HeaderCarrier
  ): Future[Date] = {
    val url = s"$FHDSSServiceUrl/fhdds/subscription/deregistration/$fhddsRegistrationNumber"
    http.post(url"$url").withBody(Json.toJson(request)).execute[Date]
  }

  def getEnrolmentProgress(implicit hc: HeaderCarrier): Future[EnrolmentProgress.EnrolmentProgress] = {
    implicit val reads = Reads.enumNameReads(EnrolmentProgress)
    val url = s"$FHDSSServiceUrl/fhdds/subscription/enrolmentProgress"
    http.get(url"$url").execute[EnrolmentProgress.EnrolmentProgress]
  }

  // $COVERAGE-OFF$
  def getAllSubmission()(implicit hc: HeaderCarrier): Future[List[SubmissionTracking]] = {
    val url = s"$FHDSSServiceUrl/fhdds/subscription/getAllSubmission"
    http.get(url"$url").execute[List[SubmissionTracking]]
  }

  def getSubMission(formBundleId: String)(implicit headerCarrier: HeaderCarrier): Future[SubmissionTracking] = {
    val url = s"$FHDSSServiceUrl/fhdds/subscription/getSubmission/$formBundleId"
    http.get(url"$url").execute[SubmissionTracking]
  }

  def deleteSubmission(formBundleId: String)(implicit hc: HeaderCarrier) = {
    val url = s"$FHDSSServiceUrl/fhdds/subscription/deleteSubmission/$formBundleId"
    http.delete(url"$url").execute[HttpResponse]
  }

  def addEnrolment(userId: String, groupId: String, regNo: String)(implicit headerCarrier: HeaderCarrier) = {
    val url = s"$FHDSSServiceUrl/fhdds/enrolment/es8/userId/$userId/groupId/$groupId/regNo/$regNo"
    http.get(url"$url").execute[HttpResponse]
  }

  def allocateEnrolment(userId: String, regNo: String)(implicit headerCarrier: HeaderCarrier) = {
    val url = s"$FHDSSServiceUrl/fhdds/enrolment/es11/userId/$userId/regNo/$regNo"
    http.get(url"$url").execute[HttpResponse]
  }

  def deleteEnrolment(userId: String, regNo: String)(implicit headerCarrier: HeaderCarrier) = {
    val url = s"$FHDSSServiceUrl/fhdds/enrolment/es12/userId/$userId/regNo/$regNo"
    http.delete(url"$url").execute[HttpResponse]
  }

  def getUserInfo(userId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = s"$FHDSSServiceUrl/fhdds/user-info/$userId"
    http.get(url"$url").execute[HttpResponse]
  }

  def getGroupInfo(groupId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = s"$FHDSSServiceUrl/fhdds/group-info/$groupId"
    http.get(url"$url").execute[HttpResponse]
  }

  def es2Info(userId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = s"$FHDSSServiceUrl/fhdds/enrolment/es2/userId/$userId"
    http.get(url"$url").execute[HttpResponse]
  }

  def es3Info(groupId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val url = s"$FHDSSServiceUrl/fhdds/enrolment/es3/groupId/$groupId"
    http.get(url"$url").execute[HttpResponse]
  }
  // $COVERAGE-ON$
}
