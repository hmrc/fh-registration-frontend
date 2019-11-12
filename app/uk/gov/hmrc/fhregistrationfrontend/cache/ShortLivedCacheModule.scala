/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.cache

import javax.inject.{Inject, Singleton}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.http.cache.client
import uk.gov.hmrc.http.cache.client.{ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.bootstrap.config.{RunMode, ServicesConfig}

class ShortLivedCacheModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[client.ShortLivedHttpCaching].to[DefaultShortLivedHttpCaching],
    bind[ShortLivedCache].to[DefaultShortLivedCache]
  )
}

@Singleton
class DefaultShortLivedCache @Inject()(
  override val shortLiveCache: ShortLivedHttpCaching,
  val runModeConfiguration: Configuration
) extends client.ShortLivedCache {
  override implicit lazy val crypto = new ApplicationCrypto(runModeConfiguration.underlying).JsonCrypto
}

@Singleton
class DefaultShortLivedHttpCaching @Inject()(
  override val http: HttpClient,
  val runModeConfiguration: Configuration,
  val runMode: RunMode,
  environment: Environment
) extends ServicesConfig(runModeConfiguration, runMode) with client.ShortLivedHttpCaching {

  override lazy val defaultSource: String =
    getConfString("cachable.short-lived-cache.journey.cache", "fh-registration-frontend")
  override lazy val baseUri: String = baseUrl("cachable.short-lived-cache")
  override lazy val domain: String = getConfString(
    "cachable.short-lived-cache.domain",
    throw new Exception(s"Could not find config 'cachable.short-lived-cache.domain'"))

}
