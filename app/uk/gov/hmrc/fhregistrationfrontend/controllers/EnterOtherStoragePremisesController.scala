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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import play.api.data.Form
import play.api.data.Forms.tuple
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.StoragePremisesForm
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{skippingOnePrefix, yesOrNo}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.StoragePremise
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation.noNavigation
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.RepeatingPageParams

import javax.inject.Inject

class EnterOtherStoragePremisesController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig)(
  cc: MessagesControllerComponents
) extends AppController(ds, cc) {

  import actions._

  val addMoreKey = "addMore"
  val elementKey = "element"

  val form: Form[(StoragePremise, Boolean)] = Form(
    tuple(
      elementKey -> skippingOnePrefix(StoragePremisesForm.storagePremiseMapping),
      addMoreKey -> yesOrNo
    )
  )

  def load(storagePremisesNum: String = "1"): Action[AnyContent] = userAction { implicit request =>
    val postAction =
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.EnterOtherStoragePremisesController.next(storagePremisesNum)

    if (config.newBusinessPartnerPagesEnabled) {
      Ok(view.storage_premise(form, noNavigation, storagePremisesNum, RepeatingPageParams(false, None), postAction))
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def next(storagePremisesNum: String = "1"): Action[AnyContent] = userAction { implicit request =>
    val postAction =
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.EnterOtherStoragePremisesController.next(storagePremisesNum)

    if (config.newBusinessPartnerPagesEnabled) {
      form.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(
            view.storage_premise(
              formWithErrors,
              noNavigation,
              storagePremisesNum,
              RepeatingPageParams(false, None),
              postAction))
        },
        result => {
          Ok(s"Form submitted with: $result")
        }
      )
    } else {
      errorHandler.errorResultsPages(Results.NotFound)
    }
  }

}
