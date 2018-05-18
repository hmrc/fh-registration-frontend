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

package uk.gov.hmrc.fhregistrationfrontend.views.registrationstatus
import play.api.mvc.Call


case class StatusPageParams(
                             status: String,
                             cta: Call,
                             secondary: Call,
                             hasSecondaryAction: Boolean,
                             category: String = "pre",
                             showSubHeading: Boolean = true,
                             showSteps: Boolean = false
                           )

object StatusPageParams {
  val statusParams = List(
    StatusPageParams(
      "received",
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw(),
      hasSecondaryAction = true,
      showSteps = true
    ),
    StatusPageParams(
      "processing",
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw(),
      hasSecondaryAction = true,
      showSteps = true
    ),
    StatusPageParams(
      "approved",
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw(),
      hasSecondaryAction = true,
      category = "post"
    ),
    StatusPageParams(
      "approvedWithConditions",
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw(),
      hasSecondaryAction = true,
      category = "post"
    ),
    StatusPageParams(
      "rejected",
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw(),
      hasSecondaryAction = false,
      showSubHeading = false,
      category = "post"
    ),
    StatusPageParams(
      "revoked",
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw(),
      hasSecondaryAction = false,
      category = "post"
    )
  )
}