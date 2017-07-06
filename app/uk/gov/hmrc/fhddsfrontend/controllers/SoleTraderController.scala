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

package uk.gov.hmrc.fhddsfrontend.controllers

import javax.inject.Inject

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.fhddsfrontend.models.Forms
import uk.gov.hmrc.fhddsfrontend.views.html.sole_trader_views._

import scala.concurrent.Future


class SoleTraderController @Inject()(ds: CommonPlayDependencies) extends AppController(ds) {

  def information(): Action[AnyContent] = authorised.async { implicit user =>
    implicit request =>
      Future.successful(Ok(inf(Forms.confirmForm)))
  }

  def submitCheckResult(): Action[AnyContent] = authorised.async { implicit user =>
    implicit request =>
      Forms.confirmForm.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(Ok(inf(formWithErrors)))
        },
        register => {
          Future.successful(Ok(summary()))
        }
      )
  }
}

