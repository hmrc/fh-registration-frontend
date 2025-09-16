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

package uk.gov.hmrc.fhregistrationfrontend.services

import javax.inject.Inject
import com.google.inject.{ImplementedBy, Singleton}
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.WithdrawalReason
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.DeregistrationReason
import uk.gov.hmrc.http.HeaderCarrier
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.cache.client.SessionCache

object KeyStoreKeys {
  val SummaryForPrintKey = "fhdds-summary-input"
  val WithdrawalReasonKey = "withdrawalReason"
  val DeregistrationReasonKey = "deregistrationReason"
}

@Singleton
class KeyStoreServiceImpl @Inject() (sessionCache: SessionCache)(implicit ec: ExecutionContext)
    extends KeyStoreService {
  import KeyStoreKeys._

  override def saveSummaryForPrint(o: String)(implicit hc: HeaderCarrier): Future[?] =
    sessionCache.cache(SummaryForPrintKey, o)

  override def fetchSummaryForPrint()(implicit hc: HeaderCarrier): Future[Option[String]] =
    sessionCache.fetchAndGetEntry[String](SummaryForPrintKey)

  override def saveWithdrawalReason(reason: WithdrawalReason)(implicit hc: HeaderCarrier): Future[?] =
    sessionCache.cache(WithdrawalReasonKey, reason)

  override def fetchWithdrawalReason()(implicit hc: HeaderCarrier): Future[Option[WithdrawalReason]] =
    sessionCache.fetchAndGetEntry[WithdrawalReason](WithdrawalReasonKey)

  override def saveDeregistrationReason(reason: DeregistrationReason)(implicit hc: HeaderCarrier): Future[?] =
    sessionCache.cache(DeregistrationReasonKey, reason)

  override def fetchDeregistrationReason()(implicit hc: HeaderCarrier): Future[Option[DeregistrationReason]] =
    sessionCache.fetchAndGetEntry[DeregistrationReason](DeregistrationReasonKey)

}

@ImplementedBy(classOf[KeyStoreServiceImpl])
trait KeyStoreService {
  def saveSummaryForPrint(o: String)(implicit hc: HeaderCarrier): Future[?]
  def fetchSummaryForPrint()(implicit hc: HeaderCarrier): Future[Option[String]]

  def saveWithdrawalReason(reason: WithdrawalReason)(implicit hc: HeaderCarrier): Future[Any]
  def fetchWithdrawalReason()(implicit hc: HeaderCarrier): Future[Option[WithdrawalReason]]

  def saveDeregistrationReason(reason: DeregistrationReason)(implicit hc: HeaderCarrier): Future[Any]
  def fetchDeregistrationReason()(implicit hc: HeaderCarrier): Future[Option[DeregistrationReason]]
}
