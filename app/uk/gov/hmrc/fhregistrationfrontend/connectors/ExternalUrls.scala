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
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@ImplementedBy(classOf[DefaultExternalUrls])
trait ExternalUrls {

  val basGatewayUrl: String
  val loginCallback: String
  val loginPath: String

  val logoutCallback: String
  val logoutPath: String

  val ggLoginUrl: String
  val continueUrl: String

  val ggLogoutUrl: String
  val logoutContinueUrl: String

  val ggOrigin: String

  val surveyRedirectUrl: String
}

class DefaultExternalUrls @Inject()(
  val runModeConfiguration: Configuration,
  environment: Environment
) extends ServicesConfig(runModeConfiguration) with ExternalUrls {

  val basGatewayUrl: String =
    getConfString("auth.bas-gateway.url", throw new RuntimeException("Bas gateway url required"))
  val loginCallback: String = getConfString("auth.login-callback.url", "/fhdds")
  val loginPath: String = getConfString("auth.login_path", "")

  val logoutCallback: String = getConfString("auth.logout-callback.url", "/fhdds/signed-out")
  val logoutPath: String = getConfString("auth.logout_path", "")

  val ggLoginUrl = s"$basGatewayUrl$loginPath"
  val continueUrl = s"$loginCallback"

  val ggLogoutUrl = s"$basGatewayUrl$logoutPath"
  val logoutContinueUrl = s"$logoutCallback"
  val ggOrigin = getString("appName")

  val surveyRedirectUrl = getConfString("surveyRedirectUrl", "/feedback/fhdds")

}
