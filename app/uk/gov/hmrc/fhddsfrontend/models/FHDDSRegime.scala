/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.fhddsfrontend.models

import uk.gov.hmrc.play.config.{RunMode, ServicesConfig}
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Accounts
import uk.gov.hmrc.play.frontend.auth.{AuthenticationProvider, GovernmentGateway, TaxRegime}

object FHDDSRegime extends TaxRegime {
  override def isAuthorised(accounts: Accounts): Boolean = true

  override def authenticationType: AuthenticationProvider = FHDDSAuthenticationProvider
}

object FHDDSAuthenticationProvider extends GovernmentGateway {
  override val continueURL: String = FHDDSExternalUrls.continueUrl
  override val loginURL: String = FHDDSExternalUrls.loginUrl
}

object FHDDSExternalUrls extends RunMode with ServicesConfig {

  private[FHDDSExternalUrls] val companyAuthHost = getConfString("auth.company-auth.url", "")
  private[FHDDSExternalUrls] val loginCallback = getConfString("auth.login-callback.url", "")
  private[FHDDSExternalUrls] val loginPath = getConfString("auth.login_path", "")

  val loginUrl = s"$companyAuthHost$loginPath"
  val continueUrl = s"$loginCallback${uk.gov.hmrc.fhddsfrontend.controllers.routes.SoleTraderController.information()}"

}
