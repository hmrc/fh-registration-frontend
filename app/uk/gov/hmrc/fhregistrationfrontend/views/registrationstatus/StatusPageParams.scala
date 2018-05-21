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
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.fhregistrationfrontend.views.html.registrationstatus._
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig

case class StatusPageParams(
                             status: String,
                             cta: Call,
                             secondary: Call,
                             nextTemplate: (StatusPageParams, Messages, AppConfig) => HtmlFormat.Appendable,
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
      nextTemplate = statusWhatHappensNext.apply,
      showSubHeading = false,
      showSteps = true
    ),
    StatusPageParams(
      "processing",
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw(),
      nextTemplate = statusWhatHappensNext.apply,
      showSubHeading = false,
      showSteps = true
    ),
    StatusPageParams(
      "approved",
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw(),
      nextTemplate = statusWhatHappensNext.apply,
      category = "post"
    ),
    StatusPageParams(
      "approvedWithConditions",
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw(),
      nextTemplate = statusWhatHappensNext.apply,
      category = "post"
    ),
    StatusPageParams(
      "rejected",
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw(),
      nextTemplate = statusNewApplication.apply,
      showSubHeading = false,
      category = "post"
    ),
    StatusPageParams(
      "revoked",
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw(),
      nextTemplate = statusNewApplication.apply,
      category = "post"
    )
  )
}