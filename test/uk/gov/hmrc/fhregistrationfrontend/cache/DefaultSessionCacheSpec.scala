/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Configuration
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.when
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class DefaultSessionCacheSpec extends AnyWordSpec with Matchers with MockitoSugar with OptionValues {

  private val http = mock[HttpClientV2]

  val configuration = Configuration(
    "cachable.short-lived-cache.journey.cache"              -> "fh-registration-frontend",
    "microservice.services.cachable.session-cache.host"     -> "localhost",
    "microservice.services.cachable.session-cache.port"     -> 8400,
    "microservice.services.cachable.session-cache.protocol" -> "keystore"
  )

  val expectedDefaultSource = configuration.get[String]("cachable.short-lived-cache.journey.cache")

  val expectedBaseUri =
    s"${configuration.get[String]("microservice.services.cachable.session-cache.protocol")}://" +
      s"${configuration.get[String]("microservice.services.cachable.session-cache.host")}:" +
      s"${configuration.get[Int]("microservice.services.cachable.session-cache.port")}"

  "defaultSource" should {
    "return fh-registration-frontend" in {

      val defaultSource =
        new DefaultSessionCache(http, configuration).defaultSource

      defaultSource shouldBe expectedDefaultSource
    }
  }

  "baseUri" should {
    "return session-cache object" in {

      val baseUri =
        new DefaultSessionCache(http, configuration).baseUri

      baseUri shouldBe expectedBaseUri

    }

    "domain" should {
      "throw new Exception " in {

        val ex = intercept[Exception] {
          new DefaultSessionCache(http, configuration).domain
        }

        ex.getMessage should include("Could not find config 'cachable.session-cache.domain'")

      }
    }
  }

}
