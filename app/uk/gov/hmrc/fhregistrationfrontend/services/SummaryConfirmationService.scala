/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.services
import services.helper.Retryable
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.DeregistrationReason
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.WithdrawalReason
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class SummaryConfirmationService @Inject() (
  fhSessionKeystoreService: KeyStoreService,
  fhSessionLocalService: SummaryConfirmationLocalService,
  fhConfig: FrontendAppConfig
) extends Retryable {

  private def featureSwitchCheck[A](expr1: => Future[A], expr2: => Future[A]): Future[A] =
    if (fhConfig.isMongoDBCacheEnabled) expr1 else expr2

  def saveSummaryForPrint(o: String)(implicit hc: HeaderCarrier): Future[Any] =
    featureSwitchCheck(
      fhSessionLocalService.saveSummaryForPrint(o),
      fhSessionKeystoreService.saveSummaryForPrint(o)
    )

  def fetchSummaryForPrint()(implicit hc: HeaderCarrier): Future[Option[String]] =
    featureSwitchCheck(
      fhSessionLocalService.fetchSummaryForPrint(),
      fhSessionKeystoreService.fetchSummaryForPrint()
    )

  def saveWithdrawalReason(reason: WithdrawalReason)(implicit hc: HeaderCarrier): Future[Any] =
    featureSwitchCheck(
      fhSessionLocalService.saveWithdrawalReason(reason),
      fhSessionKeystoreService.saveWithdrawalReason(reason)
    )

  def fetchWithdrawalReason()(implicit hc: HeaderCarrier): Future[Option[WithdrawalReason]] =
    featureSwitchCheck(
      fhSessionLocalService.fetchWithdrawalReason(),
      fhSessionKeystoreService.fetchWithdrawalReason()
    )

  def saveDeregistrationReason(
    reason: DeregistrationReason
  )(implicit hc: HeaderCarrier): Future[Any] =
    featureSwitchCheck(
      fhSessionLocalService.saveDeregistrationReason(reason),
      fhSessionKeystoreService.saveDeregistrationReason(reason)
    )

  def fetchDeregistrationReason()(implicit hc: HeaderCarrier): Future[Option[DeregistrationReason]] =
    featureSwitchCheck(
      fhSessionLocalService.fetchDeregistrationReason(),
      fhSessionKeystoreService.fetchDeregistrationReason()
    )

}
