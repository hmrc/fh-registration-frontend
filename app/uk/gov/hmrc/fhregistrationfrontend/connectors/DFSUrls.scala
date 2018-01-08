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

import uk.gov.hmrc.play.config.ServicesConfig

object DFSUrls extends ServicesConfig {
  val DFSHost: String = baseUrl("fhdds-dfs-frontend")
  val DFServiceSoleTraderFormName: String = config("fhdds-dfs-frontend").getString("sole-trader-application").getOrElse("sole-trader-application")
  val DFServiceLimitedCompanyFormName: String = config("fhdds-dfs-frontend").getString("fhdds-limited-company").getOrElse("fhdds-limited-company")

  val soleTraderUrl:String = s"$DFSHost/fhdds-forms/forms/form/$DFServiceSoleTraderFormName/new"
  val limitedCompanyUrl:String = s"$DFSHost/fhdds-forms/forms/form/$DFServiceLimitedCompanyFormName/new"

  def dfsURL(formName: String): String = {
    formName match {
      case "Individual" ⇒ soleTraderUrl
      case "Organisation" ⇒ limitedCompanyUrl
    }
  }
}
