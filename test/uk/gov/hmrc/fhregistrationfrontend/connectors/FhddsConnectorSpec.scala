/*
 * Copyright 2025 HM Revenue & Customs
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

import java.time.LocalDate
import java.util.Date
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.wordspec.AsyncWordSpec
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.fhregistration.models.fhdds.{SubmissionRequest, SubmissionResponse}
import uk.gov.hmrc.fhregistrationfrontend.models.des.{Deregistration, DeregistrationRequest, Withdrawal, WithdrawalRequest}
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus.FhddsStatus
import uk.gov.hmrc.fhregistrationfrontend.models.submissiontracking.SubmissionTracking
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FhddsConnectorSpec extends HttpClientV2Helper {

  val runModeConfiguration: Configuration = Configuration.empty
  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]

  when(mockServicesConfig.baseUrl(anyString())).thenReturn("http://localhost:8080")

  val connector = new FhddsConnector(mockHttp, runModeConfiguration)(using global) {
    override def baseUrl(serviceName: String): String = mockServicesConfig.baseUrl(serviceName)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()
  "FhddsConnector" when {

    "getStatus" should {
      "return FhddsStatus" in {
        val status = FhddsStatus.Approved
        requestBuilderExecute(Future.successful(Json.toJson(status).as[FhddsStatus]))
        connector.getStatus("12345").futureValue shouldBe status
      }
    }

    "createSubmission" should {
      "return SubmissionResponse" in {
        val response = SubmissionResponse("formBundleId", new Date())
        val submissionData = Json.obj("test" -> "data")
        val request = SubmissionRequest("test@example.com", submissionData)
        requestBuilderExecute(Future.successful(Json.toJson(response).as[SubmissionResponse]))
        connector.createSubmission("safeId", None, request).futureValue shouldBe response
        jsonCaptor.getValue shouldBe Json.toJson(request)
      }

      "return SubmissionResponse with currentRegNumber" in {
        val response = SubmissionResponse("formBundleId", new Date())
        val submissionData = Json.obj("test" -> "data")
        val request = SubmissionRequest("test@example.com", submissionData)
        requestBuilderExecute(Future.successful(Json.toJson(response).as[SubmissionResponse]))
        connector.createSubmission("safeId", Some("regNumber"), request).futureValue shouldBe response
        jsonCaptor.getValue shouldBe Json.toJson(request)
      }
    }

    "amendSubmission" should {
      "return SubmissionResponse" in {
        val response = SubmissionResponse("formBundleId", new Date())
        val submissionData = Json.obj("test" -> "data")
        val request = SubmissionRequest("test@example.com", submissionData)
        requestBuilderExecute(Future.successful(Json.toJson(response).as[SubmissionResponse]))
        connector.amendSubmission("12345", request).futureValue shouldBe response
        jsonCaptor.getValue shouldBe Json.toJson(request)
      }
    }

    "withdraw" should {
      "return Date" in {
        val date = new Date()
        val withdrawal = Withdrawal(LocalDate.now(), "reason", Some("otherReason"))
        val request = WithdrawalRequest("test@example.com", withdrawal)
        requestBuilderExecute(Future.successful(date))
        connector.withdraw("12345", request).futureValue shouldBe date
        jsonCaptor.getValue shouldBe Json.toJson(request)
      }
    }

    "deregister" should {
      "return Date" in {
        val date = new Date()
        val deregistration = Deregistration(LocalDate.now(), "reason", Some("otherReason"))
        val request = DeregistrationRequest("test@example.com", deregistration)
        requestBuilderExecute(Future.successful(date))
        connector.deregister("12345", request).futureValue shouldBe date
        jsonCaptor.getValue shouldBe Json.toJson(request)
      }
    }

    "getEnrolmentProgress" should {
      "return EnrolmentProgress" in {
        val progress = EnrolmentProgress.Pending
        requestBuilderExecute(Future.successful(Json.toJson(progress).as[EnrolmentProgress.EnrolmentProgress]))
        connector.getEnrolmentProgress.futureValue shouldBe progress
      }
    }

    "getAllSubmission" should {
      "return a list of SubmissionTracking" in {
        val submissionTime = new Date().getTime
        val progress = EnrolmentProgress.Pending

        val submission = SubmissionTracking(
          "userId",
          "formBundleId",
          "email",
          submissionTime,
          Some(progress),
          Some("regNumber")
        )

        val submissions = List(submission)

        requestBuilderExecute(Future.successful(submissions))
        connector.getAllSubmission().futureValue shouldBe submissions
      }
    }

    "getSubMission" should {
      "return SubmissionTracking" in {
        val submissionTime = new Date().getTime
        val progress = EnrolmentProgress.Pending

        val submission = SubmissionTracking(
          "userId",
          "formBundleId",
          "email",
          submissionTime,
          Some(progress),
          Some("regNumber")
        )

        requestBuilderExecute(Future.successful(submission))
        connector.getSubMission("formBundleId").futureValue shouldBe submission
      }
    }

    "deleteSubmission" should {
      "return HttpResponse" in {
        val response = HttpResponse(200, "")
        requestBuilderExecute(Future.successful(response))
        connector.deleteSubmission("formBundleId").futureValue shouldBe response
      }
    }

    "addEnrolment" should {
      "return HttpResponse" in {
        val response = HttpResponse(200, "")
        requestBuilderExecute(Future.successful(response))
        connector.addEnrolment("userId", "groupId", "regNo").futureValue shouldBe response
      }
    }

    "allocateEnrolment" should {
      "return HttpResponse" in {
        val response = HttpResponse(200, "")
        requestBuilderExecute(Future.successful(response))
        connector.allocateEnrolment("userId", "regNo").futureValue shouldBe response
      }
    }

    "deleteEnrolment" should {
      "return HttpResponse" in {
        val response = HttpResponse(200, "")
        requestBuilderExecute(Future.successful(response))
        connector.deleteEnrolment("userId", "regNo").futureValue shouldBe response
      }
    }

    "getUserInfo" should {
      "return HttpResponse" in {
        val response = HttpResponse(200, "")
        requestBuilderExecute(Future.successful(response))
        connector.getUserInfo("userId").futureValue shouldBe response
      }
    }

    "getGroupInfo" should {
      "return HttpResponse" in {
        val response = HttpResponse(200, "")
        requestBuilderExecute(Future.successful(response))
        connector.getGroupInfo("groupId").futureValue shouldBe response
      }
    }

    "es2Info" should {
      "return HttpResponse" in {
        val response = HttpResponse(200, "")
        requestBuilderExecute(Future.successful(response))
        connector.es2Info("userId").futureValue shouldBe response
      }
    }

    "es3Info" should {
      "return HttpResponse" in {
        val response = HttpResponse(200, "")
        requestBuilderExecute(Future.successful(response))
        connector.es3Info("groupId").futureValue shouldBe response
      }
    }
  }
}
