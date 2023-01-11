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

package uk.gov.hmrc.fhregistrationfrontend.teststubs

import uk.gov.hmrc.fhregistrationfrontend.connectors.ExternalUrls

object StubbedExternalUrls extends ExternalUrls {
  override val basGatewayUrl: String = "company/auth"
  override val loginCallback: String = "/fhdds"
  override val loginPath: String = "login/path"
  override val logoutCallback: String = "/fhdds/signed-out"
  override val logoutPath: String = "/fhdds/sign-out"
  override val ggLoginUrl: String = s"$basGatewayUrl$loginPath"
  override val continueUrl: String = s"$loginCallback"
  override val ggLogoutUrl: String = s"$basGatewayUrl$logoutPath"
  override val logoutContinueUrl: String = logoutCallback
  override val surveyRedirectUrl: String = "/feedback-survey?origin=fhdds"
  override val ggOrigin: String = "FHDDS"
}
