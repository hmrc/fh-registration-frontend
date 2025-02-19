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

package uk.gov.hmrc.fhregistrationfrontend.views.registrationstatus
import javax.inject.Inject
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus.FhddsStatus
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus._

case class StatusPageParams(
  status: FhddsStatus,
  cta: Call,
  secondary: Option[Call],
  nextTemplate: (StatusPageParams, Messages, AppConfig) => HtmlFormat.Appendable,
  category: String = "pre",
  showSubHeading: Boolean = true,
  showSteps: Boolean = false
)

object StatusPageParams {

  class StatusParams @Inject() (registrationStatusViews: RegistrationStatusViews) {

    val statusParams = List(
      StatusPageParams(
        Received,
        cta = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
        secondary = Some(uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw),
        nextTemplate = registrationStatusViews.statusWhatHappensNext.apply,
        showSubHeading = false
      ),
      StatusPageParams(
        Processing,
        cta = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startAmendment(),
        secondary = Some(uk.gov.hmrc.fhregistrationfrontend.controllers.routes.WithdrawalController.startWithdraw),
        nextTemplate = registrationStatusViews.statusWhatHappensNext.apply,
        showSubHeading = false
      ),
      StatusPageParams(
        Approved,
        cta = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startVariation(),
        secondary =
          Some(uk.gov.hmrc.fhregistrationfrontend.controllers.routes.DeregistrationController.startDeregister),
        nextTemplate = registrationStatusViews.statusWhatHappensNext.apply,
        category = "post"
      ),
      StatusPageParams(
        ApprovedWithConditions,
        cta = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.AmendmentController.startVariation(),
        secondary =
          Some(uk.gov.hmrc.fhregistrationfrontend.controllers.routes.DeregistrationController.startDeregister),
        nextTemplate = registrationStatusViews.statusWhatHappensNext.apply,
        category = "post"
      ),
      StatusPageParams(
        Rejected,
        cta = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.Application.startOrContinueApplication(),
        secondary = Some(uk.gov.hmrc.fhregistrationfrontend.controllers.routes.ReadOnlySummaryController.view()),
        nextTemplate = registrationStatusViews.statusNewApplication.apply,
        showSubHeading = false,
        category = "post"
      ),
      StatusPageParams(
        Revoked,
        cta = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.Application.startOrContinueApplication(),
        secondary = Some(uk.gov.hmrc.fhregistrationfrontend.controllers.routes.ReadOnlySummaryController.view()),
        nextTemplate = registrationStatusViews.statusNewApplication.apply,
        category = "post"
      ),
      StatusPageParams(
        Withdrawn,
        cta = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.Application.startOrContinueApplication(),
        secondary = None,
        nextTemplate = registrationStatusViews.statusWithdrawn.apply,
        showSubHeading = false
      ),
      StatusPageParams(
        Deregistered,
        cta = uk.gov.hmrc.fhregistrationfrontend.controllers.routes.Application.startOrContinueApplication(),
        secondary = Some(uk.gov.hmrc.fhregistrationfrontend.controllers.routes.ReadOnlySummaryController.view()),
        nextTemplate = registrationStatusViews.statusNewApplication.apply,
        category = "no-notification"
      )
    )
    def apply(status: FhddsStatus): Option[StatusPageParams] = statusParams find (_.status == status)
  }
}
