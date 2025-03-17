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

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.Json
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig
import uk.gov.hmrc.fhregistrationfrontend.models.emailverification.{Email, EmailVerificationRequest}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class DefaultEmailVerificationConnectorSpec
    extends AsyncWordSpec with Matchers with MockitoSugar with ScalaFutures with HttpClientV2Helper {

  val appConfig: AppConfig = mock[AppConfig]
  val runModeConfiguration: Configuration = Configuration.empty
  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]

  when(mockServicesConfig.baseUrl(anyString())).thenReturn("http://localhost:8080")

  val connector = new DefaultEmailVerificationConnector(appConfig, mockHttp, runModeConfiguration)(global) {
    override def baseUrl(serviceName: String): String = mockServicesConfig.baseUrl(serviceName)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()
  "DefaultEmailVerificationConnector" when {

    "isVerified" should {

      "return true when the email is verified" in {
        requestBuilderExecute(Future.successful(HttpResponse(200, "")))
        connector.isVerified("test@example.com").futureValue shouldBe true
        val expectedJson = Json.parse("""{"email":"test@example.com"}""")
        jsonCaptor.getValue shouldBe expectedJson
      }

      "return false when the email is not verified" in {
        requestBuilderExecute(Future.successful(HttpResponse(404, "")))
        connector.isVerified("test@example.com").futureValue shouldBe false
        val expectedJson = Json.parse("""{"email":"test@example.com"}""")
        jsonCaptor.getValue shouldBe expectedJson
      }

      "throw UpstreamErrorResponse for 4xx errors other than 404" in {
        requestBuilderExecute(Future.successful(HttpResponse(400, "")))
        recoverToSucceededIf[UpstreamErrorResponse] {
          connector.isVerified("test@example.com")
        }
      }

      "throw UpstreamErrorResponse for 5xx errors" in {
        requestBuilderExecute(Future.successful(HttpResponse(500, "")))
        recoverToSucceededIf[UpstreamErrorResponse] {
          connector.isVerified("test@example.com")
        }
      }
    }

    "requestVerification" should {

      "return true when verification request is successful with 409" in {
        requestBuilderExecute(Future.successful(HttpResponse(409, "")))
        when(appConfig.emailVerificationCallback("testHash")).thenReturn("http://callback.com/testHash")
        connector.requestVerification("test@example.com", "testHash").futureValue shouldBe true
        val expectedJson = Json.parse(
          """{
            |"email":"test@example.com",
            |"templateId":"fhdds_email_verification",
            |"templateParameters":{},
            |"linkExpiryDuration":"PT30M",
            |"continueUrl":"http://callback.com/testHash"
            |}""".stripMargin
        )
        jsonCaptor.getValue shouldBe expectedJson
      }

      "return true when verification request is successful with 201" in {
        requestBuilderExecute(Future.successful(HttpResponse(201, "")))
        when(appConfig.emailVerificationCallback("testHash")).thenReturn("http://callback.com/testHash")
        connector.requestVerification("test@example.com", "testHash").futureValue shouldBe true
        val expectedJson = Json.parse(
          """{
            |"email":"test@example.com",
            |"templateId":"fhdds_email_verification",
            |"templateParameters":{},
            |"linkExpiryDuration":"PT30M",
            |"continueUrl":"http://callback.com/testHash"
            |}""".stripMargin
        )
        jsonCaptor.getValue shouldBe expectedJson
      }

      "throw UpstreamErrorResponse for 4xx errors other than 409" in {
        requestBuilderExecute(Future.failed(UpstreamErrorResponse("test", 400, 500)))
        when(appConfig.emailVerificationCallback("testHash")).thenReturn("http://callback.com/testHash")
        recoverToSucceededIf[UpstreamErrorResponse] {
          connector.requestVerification("test@example.com", "testHash")
        }
      }

      "throw UpstreamErrorResponse for 5xx errors" in {
        requestBuilderExecute(Future.failed(UpstreamErrorResponse("test", 500, 502)))
        when(appConfig.emailVerificationCallback("testHash")).thenReturn("http://callback.com/testHash")
        recoverToSucceededIf[UpstreamErrorResponse] {
          connector.requestVerification("test@example.com", "testHash")
        }
      }
    }
  }
}
