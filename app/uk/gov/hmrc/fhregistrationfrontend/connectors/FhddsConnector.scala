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
import play.api.libs.json.Reads
import play.api.{Configuration, Environment}
import uk.gov.hmrc.fhregistration.models.fhdds.{SubmissionRequest, SubmissionResponse}
import uk.gov.hmrc.fhregistrationfrontend.models.des.{DeregistrationRequest, SubscriptionDisplayWrapper, WithdrawalRequest}
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus.FhddsStatus
import uk.gov.hmrc.fhregistrationfrontend.models.submissiontracking.SubmissionTracking
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FhddsConnector @Inject() (
  val http: HttpClientV2,
  val runModeConfiguration: Configuration,
  environment: Environment
)(implicit ec: ExecutionContext)
    extends ServicesConfig(runModeConfiguration) {
  val FHDSSServiceUrl: String = baseUrl("fhdds")

  def getStatus(fhddsRegistrationNumber: String)(implicit headerCarrier: HeaderCarrier): Future[FhddsStatus] = {
//    http.GET[FhddsStatus](s"$FHDSSServiceUrl/fhdds/subscription/$fhddsRegistrationNumber/status")
    val url = s"$FHDSSServiceUrl/fhdds/subscription/$fhddsRegistrationNumber/status"
    http.get(url"$url").execute[FhddsStatus]
  }

  def getSubmission(fhddsRegistrationNumber: String)(implicit
    headerCarrier: HeaderCarrier
  ): Future[SubscriptionDisplayWrapper] = {
//    http.GET[SubscriptionDisplayWrapper](s"$FHDSSServiceUrl/fhdds/subscription/$fhddsRegistrationNumber/get")
    val url = s"$FHDSSServiceUrl/fhdds/subscription/$fhddsRegistrationNumber/get"
    http.get(url"$url").execute[SubscriptionDisplayWrapper]
  }

  def createSubmission(safeId: String, currentRegNumber: Option[String], request: SubmissionRequest)(implicit
    headerCarrier: HeaderCarrier
  ): Future[SubmissionResponse] = {
    val url = currentRegNumber.fold(
      s"$FHDSSServiceUrl/fhdds/subscription/subscribe/$safeId"
    ) { currentRegNumber =>
      s"$FHDSSServiceUrl/fhdds/subscription/subscribe/$safeId?currentRegNumber=$currentRegNumber"
    }

//    http.POST[SubmissionRequest, SubmissionResponse](url, request)
    http.post(url"$url").withBody[SubmissionRequest](request).execute[SubmissionResponse]
  }

  def amendSubmission(fhddsRegistrationNumber: String, request: SubmissionRequest)(implicit
    headerCarrier: HeaderCarrier
  ): Future[SubmissionResponse] = {
//    http.POST[SubmissionRequest, SubmissionResponse](
//      s"$FHDSSServiceUrl/fhdds/subscription/amend/$fhddsRegistrationNumber",
//      request
//    )
    val url = s"$FHDSSServiceUrl/fhdds/subscription/amend/$fhddsRegistrationNumber"
    http.post(url"$url").withBody[SubmissionRequest](request).execute[SubmissionResponse]
  }

  def withdraw(fhddsRegistrationNumber: String, request: WithdrawalRequest)(implicit
    headerCarrier: HeaderCarrier
  ): Future[Date] = {
//    http.POST[WithdrawalRequest, Date](
//      s"$FHDSSServiceUrl/fhdds/subscription/withdrawal/$fhddsRegistrationNumber",
//      request
//    )
    val url = s"$FHDSSServiceUrl/fhdds/subscription/withdrawal/$fhddsRegistrationNumber"
    http.post(url"$url").withBody[WithdrawalRequest](request).execute[Date]
  }

  def deregister(fhddsRegistrationNumber: String, request: DeregistrationRequest)(implicit
    headerCarrier: HeaderCarrier
  ): Future[Date] = {
//    http.POST[DeregistrationRequest, Date](
//      s"$FHDSSServiceUrl/fhdds/subscription/deregistration/$fhddsRegistrationNumber",
//      request
//    )
    val url = s"$FHDSSServiceUrl/fhdds/subscription/deregistration/$fhddsRegistrationNumber"
    http.post(url"$url").withBody[DeregistrationRequest](request).execute[Date]
  }

  def getEnrolmentProgress(implicit hc: HeaderCarrier): Future[EnrolmentProgress.EnrolmentProgress] = {
    implicit val reads = Reads.enumNameReads(EnrolmentProgress)
//    http.GET[EnrolmentProgress.EnrolmentProgress](s"$FHDSSServiceUrl/fhdds/subscription/enrolmentProgress")
    val url = "$FHDSSServiceUrl/fhdds/subscription/enrolmentProgress"
    http.get(url"$url").execute[EnrolmentProgress.EnrolmentProgress]
  }

  // $COVERAGE-OFF$
  def getAllSubmission()(implicit hc: HeaderCarrier): Future[List[SubmissionTracking]] = {
//    http.GET[List[SubmissionTracking]](s"$FHDSSServiceUrl/fhdds/subscription/getAllSubmission")
    val url = s"$FHDSSServiceUrl/fhdds/subscription/getAllSubmission"
    http.get(url"$url").execute[List[SubmissionTracking]]
  }

  def getSubMission(formBundleId: String)(implicit headerCarrier: HeaderCarrier): Future[SubmissionTracking] = {
//    http.GET[SubmissionTracking](s"$FHDSSServiceUrl/fhdds/subscription/getSubmission/$formBundleId")
    val url = s"$FHDSSServiceUrl/fhdds/subscription/getSubmission/$formBundleId"
    http.get(url"$url").execute[SubmissionTracking]
  }

  def deleteSubmission(formBundleId: String)(implicit hc: HeaderCarrier) = {
//    http.DELETE[HttpResponse](s"$FHDSSServiceUrl/fhdds/subscription/deleteSubmission/$formBundleId")
    val url = s"$FHDSSServiceUrl/fhdds/subscription/deleteSubmission/$formBundleId"
    http.delete(url"$url").execute[HttpResponse]
  }

  def addEnrolment(userId: String, groupId: String, regNo: String)(implicit headerCarrier: HeaderCarrier) = {
//    http.GET[HttpResponse](s"$FHDSSServiceUrl/fhdds/enrolment/es8/userId/$userId/groupId/$groupId/regNo/$regNo")
    val url = s"$FHDSSServiceUrl/fhdds/enrolment/es8/userId/$userId/groupId/$groupId/regNo/$regNo"
    http.get(url"$url").execute[HttpResponse]
  }

  def allocateEnrolment(userId: String, regNo: String)(implicit headerCarrier: HeaderCarrier) = {
//    http.GET[HttpResponse](s"$FHDSSServiceUrl/fhdds/enrolment/es11/userId/$userId/regNo/$regNo ")
    val url = s"$FHDSSServiceUrl/fhdds/enrolment/es11/userId/$userId/regNo/$regNo"
    http.get(url"$url").execute[HttpResponse]
  }

  def deleteEnrolment(userId: String, regNo: String)(implicit headerCarrier: HeaderCarrier) = {
//    http.DELETE[HttpResponse](s"$FHDSSServiceUrl/fhdds/enrolment/es12/userId/$userId/regNo/$regNo ")
    val url = s"$FHDSSServiceUrl/fhdds/enrolment/es12/userId/$userId/regNo/$regNo"
    http.delete(url"$url").execute[HttpResponse]
  }

  def getUserInfo(userId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
//    http.GET[HttpResponse](s"$FHDSSServiceUrl/fhdds/user-info/$userId")
    val url = s"$FHDSSServiceUrl/fhdds/user-info/$userId"
    http.get(url"$url").execute[HttpResponse]
  }

  def getGroupInfo(groupId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
//    http.GET[HttpResponse](s"$FHDSSServiceUrl/fhdds/group-info/$groupId")
    val url = s"$FHDSSServiceUrl/fhdds/group-info/$groupId"
    http.get(url"$url").execute[HttpResponse]
  }

  def es2Info(userId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
//    http.GET[HttpResponse](s"$FHDSSServiceUrl/fhdds/enrolment/es2/userId/$userId")
    val url = s"$FHDSSServiceUrl/fhdds/enrolment/es2/userId/$userId"
    http.get(url"$url").execute[HttpResponse]
  }

  def es3Info(groupId: String)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
//    http.GET[HttpResponse](s"$FHDSSServiceUrl/fhdds/enrolment/es3/groupId/$groupId")
    val url = s"$FHDSSServiceUrl/fhdds/enrolment/es3/groupId/$groupId"
    http.get(url"$url").execute[HttpResponse]
  }
  // $COVERAGE-ON$
}
