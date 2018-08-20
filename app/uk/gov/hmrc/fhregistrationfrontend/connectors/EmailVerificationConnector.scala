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

import javax.inject.Inject

import com.google.inject.ImplementedBy
import play.api.{Configuration, Environment}
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig
import uk.gov.hmrc.fhregistrationfrontend.models.emailverification.EmailVerificationRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.Future
import uk.gov.hmrc.play.http.logging.MdcLoggingExecutionContext._
import uk.gov.hmrc.http._


@ImplementedBy(classOf[DefaultEmailVerificationConnector])
trait EmailVerificationConnector {

  def isVerified(email: String)(implicit headerCarrier: HeaderCarrier): Future[Boolean]
  def requestVerification(email: String, emailHash: String)(implicit headerCarrier: HeaderCarrier): Future[Boolean]

}


class DefaultEmailVerificationConnector @Inject() (
  appConfig: AppConfig,
  val http: HttpClient,
  override val runModeConfiguration: Configuration,
  environment: Environment
) extends ServicesConfig
  with EmailVerificationConnector
  with HttpErrorFunctions
{

  override protected def mode = environment.mode

  val emailVerificationBaseUrl = s"${baseUrl("email-verification")}/email-verification"

  override def isVerified(email: String)(implicit headerCarrier: HeaderCarrier): Future[Boolean] = {
    val url = s"$emailVerificationBaseUrl/verified-email-addresses/$email"
    implicit val customReads = new HttpReads[Boolean] {
      override def read(method: String, url: String, response: HttpResponse): Boolean = {
        response.status match {
          case status if status == 200 ⇒ true
          case status if status == 404 ⇒ false
          case status if is4xx(status) ⇒ throw Upstream4xxResponse("email-verification/verified-email-addresses error", response.status, 500)
          case status if is5xx(status) ⇒ throw Upstream5xxResponse("email-verification/verified-email-addresses error", response.status, 502)
        }
      }
    }

    http.GET[Boolean](url)
  }

  override def requestVerification(email: String, emailHash: String)(implicit headerCarrier: HeaderCarrier): Future[Boolean] = {
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
      override def read(method: String, url: String, response: HttpResponse): Boolean = {
        response.status match {
          case status if status == 409 ⇒ true
          case status if status == 201 ⇒ false
          case status if is4xx(status) ⇒ throw Upstream4xxResponse("email-verification/verification-requests error", response.status, 500)
          case status if is5xx(status) ⇒ throw Upstream5xxResponse("email-verification/verification-requests error", response.status, 502)

        }
      }
    }

    val url = s"$emailVerificationBaseUrl/verification-requests"
    http.POST[EmailVerificationRequest, Boolean](url, request)
  }
}