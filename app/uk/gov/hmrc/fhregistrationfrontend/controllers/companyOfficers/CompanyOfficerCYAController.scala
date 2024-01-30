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

package uk.gov.hmrc.fhregistrationfrontend.controllers.companyOfficers

import com.google.inject.Inject
import models.{Mode, NormalMode}
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.controllers.{AppController, CommonPlayDependencies}
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import viewmodels.govuk.SummaryListFluency

class CompanyOfficerCYAController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(cc: MessagesControllerComponents)
    extends AppController(ds, cc) with SummaryListFluency {

  import actions._

  def load(index: Int): Action[AnyContent] = dataRequiredAction(index, NormalMode) { implicit request =>
    val summaryList = SummaryListViewModel(
      rows = Seq.empty
    )
    Ok(view.companyOfficersCYAView("#", summaryList, routes.CompanyOfficerCYAController.next(index)))
  }

  def next(index: Int): Action[AnyContent] = dataRequiredAction(index, NormalMode) { implicit request =>
    Ok(s"Form submitted, with result:")
  }
}
