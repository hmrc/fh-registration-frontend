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

import com.google.common.base.Charsets
import com.google.common.io.BaseEncoding
import uk.gov.hmrc.play.config.ServicesConfig

object CompaniesHouseConfig extends ServicesConfig {

  val authHeader = s"Basic ${encodeAuthCode(getCompaniesHouseAuthCode)}"
  val url = config("companies-house").getString("api-key").getOrElse("url")

  def getCompaniesHouseAuthCode: String = {
    config("companies-house").getString("api-key").getOrElse("")
  }

  def encodeAuthCode(code :String): String = {
    BaseEncoding.base64().encode(code.getBytes(Charsets.UTF_8))
  }
}
