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
import uk.gov.hmrc.fhddsfrontend.connectors.DESConnector
import uk.gov.hmrc.fhddsfrontend.models.{BusinessMatchingModels, DFSURL, Forms}
import uk.gov.hmrc.fhddsfrontend.views.html.sole_trader_views._

import scala.concurrent.Future


class SoleTraderController @Inject()(ds: CommonPlayDependencies, desConnector:DESConnector)
  extends AppController(ds, desConnector) {

  def information(): Action[AnyContent] = authorisedUser {
    implicit request ⇒
      implicit userEnrolments ⇒
        BusinessMatchingModels.getBusinessDetail(userEnrolments, desConnector).map {
          case Some((address, org)) ⇒ Ok(inf(Forms.confirmForm, address, org.organisationName))
          case _ ⇒ Redirect(DFSURL.DFServiceSoleTraderUrl)
        }
  }

  def submitCheckResult(): Action[AnyContent] = authorisedUser {
    implicit request ⇒
      implicit userEnrolments ⇒
          Forms.confirmForm.bindFromRequest().fold(
            formWithErrors => {
              BusinessMatchingModels.getBusinessDetail(userEnrolments, desConnector).map {
                case Some((address, org)) ⇒ Ok(inf(formWithErrors, address, org.organisationName))
                case _ ⇒ Redirect(DFSURL.DFServiceSoleTraderUrl)
              }
            },
            register => {
              if (register.value) Future.successful(Redirect(DFSURL.DFServiceSoleTraderUrl))
              else Future.successful(Redirect(DFSURL.DFServiceSoleTraderUrl))
            }
          )
  }
}