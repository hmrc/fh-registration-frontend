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

package uk.gov.hmrc.fhregistrationfrontend.cache

import javax.inject.{Inject, Singleton}
import play.api.inject.{Binding, Module}
import play.api.{Configuration, Environment}
import uk.gov.hmrc.crypto.{ApplicationCrypto, Decrypter, Encrypter}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.cache.client
import uk.gov.hmrc.http.cache.client.{ShortLivedCache, ShortLivedHttpCaching}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class ShortLivedCacheModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[client.ShortLivedHttpCaching].to[DefaultShortLivedHttpCaching],
    bind[ShortLivedCache].to[DefaultShortLivedCache]
  )
}

@Singleton
class DefaultShortLivedCache @Inject() (
  override val shortLiveCache: ShortLivedHttpCaching,
  val runModeConfiguration: Configuration
) extends client.ShortLivedCache {
  override implicit val crypto: Encrypter with Decrypter = new ApplicationCrypto(
    runModeConfiguration.underlying
  ).JsonCrypto
}

@Singleton
class DefaultShortLivedHttpCaching @Inject() (
  val http: HttpClientV2,
  val runModeConfiguration: Configuration
) extends ServicesConfig(runModeConfiguration) with client.ShortLivedHttpCaching {

  override lazy val defaultSource: String =
    getConfString("cachable.short-lived-cache.journey.cache", "fh-registration-frontend")
  override lazy val baseUri: String = baseUrl("cachable.short-lived-cache")
  override lazy val domain: String = getConfString(
    "cachable.short-lived-cache.domain",
    throw new Exception(s"Could not find config 'cachable.short-lived-cache.domain'")
  )

  override def httpClientV2: HttpClientV2 = http
}
