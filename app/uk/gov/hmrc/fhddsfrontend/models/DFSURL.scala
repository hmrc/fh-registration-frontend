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

import uk.gov.hmrc.play.config.ServicesConfig

object DFSURL extends ServicesConfig {
  val DFSHost: String = config("fhdds-dfs-frontend").getString("host").getOrElse("")
  val DFServiceSoleTraderFormName: String = config("fhdds-dfs-frontend").getString("sole-trader-application").getOrElse("")
  val DFServiceLimitedCompanyFormName: String = config("fhdds-dfs-frontend").getString("limited-company-application").getOrElse("")

  val soleTraderUrl:String = s"$DFSHost/fhdds-forms/forms/form/$DFServiceSoleTraderFormName/new"
  val limitedCompanyUrl:String = s"$DFSHost/fhdds-forms/forms/form/$DFServiceLimitedCompanyFormName/new"

  def dfsURL(formName: String): String = {
    formName match {
      case "Individual" ⇒ soleTraderUrl
      case "Organisation" ⇒ limitedCompanyUrl
    }
  }
}
