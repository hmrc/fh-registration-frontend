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

import javax.inject.Inject
import com.google.inject.ImplementedBy
import play.api.libs.json.Json
import play.api.{Configuration, Environment}
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig
import uk.gov.hmrc.fhregistrationfrontend.models.emailverification.{Email, EmailVerificationRequest}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.http.HttpReads.Implicits._
import play.api.libs.ws.writeableOf_JsValue

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.client.HttpClientV2

@ImplementedBy(classOf[DefaultEmailVerificationConnector])
trait EmailVerificationConnector {
  def isVerified(email: String)(implicit headerCarrier: HeaderCarrier): Future[Boolean]
  def requestVerification(email: String, emailHash: String)(implicit headerCarrier: HeaderCarrier): Future[Boolean]
}

class DefaultEmailVerificationConnector @Inject() (
  appConfig: AppConfig,
  val http: HttpClientV2,
  val runModeConfiguration: Configuration
)(implicit ec: ExecutionContext)
    extends ServicesConfig(runModeConfiguration) with EmailVerificationConnector with HttpErrorFunctions {
  val emailVerificationBaseUrl = s"${baseUrl("email-verification")}/email-verification"

  override def isVerified(email: String)(implicit headerCarrier: HeaderCarrier): Future[Boolean] = {
    val url = s"$emailVerificationBaseUrl/verified-email-check"

    http
      .post(url"$url")
      .withBody(Json.toJson(Email(email)))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case status if status == 200 => true
          case status if status == 404 => false
          case status if is4xx(status) =>
            throw UpstreamErrorResponse("email-verification/verified-email-check error", response.status, 500)
          case status if is5xx(status) =>
            throw UpstreamErrorResponse("email-verification/verified-email-check error", response.status, 502)
        }
      }
  }

  override def requestVerification(email: String, emailHash: String)(implicit
    headerCarrier: HeaderCarrier
  ): Future[Boolean] = {
    val templateId: String = "fhdds_email_verification"
    val linkExpiryDuration: String = "PT30M"
    val request = EmailVerificationRequest(
      email,
      templateId,
      templateParameters = Map.empty,
      linkExpiryDuration = linkExpiryDuration,
      continueUrl = appConfig.emailVerificationCallback(emailHash)
    )

    implicit val customReads = new HttpReads[Boolean] {
      override def read(method: String, url: String, response: HttpResponse): Boolean =
        response.status match {
          case status if status == 409 => true
          case status if status == 201 => false
          case status if is4xx(status) =>
            throw UpstreamErrorResponse("email-verification/verification-requests error", response.status, 500)
          case status if is5xx(status) =>
            throw UpstreamErrorResponse("email-verification/verification-requests error", response.status, 502)

        }
    }

    val url = s"$emailVerificationBaseUrl/verification-requests"
    http
      .post(url"$url")
      .withBody(Json.toJson(request))
      .execute[HttpResponse]
      .map(_ => true)
  }
}
