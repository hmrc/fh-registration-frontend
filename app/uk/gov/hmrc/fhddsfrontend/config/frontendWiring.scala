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

package uk.gov.hmrc.fhddsfrontend.config

import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.auth.otac.PlayOtacAuthConnector
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.frontend.config.LoadAuditingConfig
import uk.gov.hmrc.play.http.ws._

object FrontendAuditConnector extends AuditConnector with RunMode {
  lazy val auditingConfig: AuditingConfig = LoadAuditingConfig(s"$env.auditing")
}

trait Hooks extends HttpHooks with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override lazy val auditConnector: AuditConnector = FrontendAuditConnector
}

trait WSHttp extends
  HttpGet with WSGet with
  HttpPut with WSPut with
  HttpPatch with WSPatch with
  HttpPost with WSPost with
  HttpDelete with WSDelete with
  Hooks with AppName

object WSHttp extends WSHttp with HttpAuditing {
  override lazy val auditConnector = FrontendAuditConnector
}

object FrontendAuthConnector extends PlayAuthConnector with ServicesConfig with WSHttp {
  override val serviceUrl: String = baseUrl("auth")
  override def http: CorePost = WSHttp
//  override val serviceUrl: String = baseUrl("auth")
//
//  override def http = WSHttp
}

object ConcreteOtacAuthConnector extends PlayOtacAuthConnector with ServicesConfig  {
  val serviceUrl: String = baseUrl("auth")
  val http: CoreGet = WSHttp
}

