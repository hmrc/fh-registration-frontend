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
import uk.gov.hmrc.fhregistrationfrontend.repositories.SummaryConfirmationRepository
import services.helper.Retryable
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.DeregistrationReason
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.WithdrawalReason
import uk.gov.hmrc.fhregistrationfrontend.models.SummaryConfirmation
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}
import javax.inject.Inject

class SummaryConfirmationLocalService @Inject() (
  sessionRepository: SummaryConfirmationRepository,
  FHDDSConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends Retryable {

  private def cleanFHDDSSessionCache(
    id: String,
    summaryForPrintKey: Option[String] = None,
    withdrawalReason: Option[WithdrawalReason] = None,
    deregistrationReason: Option[DeregistrationReason] = None
  ): SummaryConfirmation =
    SummaryConfirmation(id, summaryForPrintKey, withdrawalReason, deregistrationReason)

  private def getSummaryId(implicit hc: HeaderCarrier): String =
    hc.sessionId match {
      case Some(id) => id.value
      case None     => throw new RuntimeException("Unexpected error, No session id found")
    }

  def saveSummaryForPrint(o: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    logger.info("[SummaryConfirmationService][storeSummaryForPrintKey]: Storing Summary For Print Key to Session...")

    val fUpdatedCache = sessionRepository.get(getSummaryId) map {
      case Some(summaryConfirmation) => summaryConfirmation.copy(summaryForPrintKey = Some(o))
      case None                      => cleanFHDDSSessionCache(getSummaryId, summaryForPrintKey = Some(o))
    }

    fUpdatedCache.flatMap { updatedCache =>
      setSessionCacheDataWithRetry(updatedCache)
        .map(_ => updatedCache.summaryForPrintKey)
        .recover { case e: Throwable =>
          logger.warn(
            "[SummaryConfirmationService][storeSummaryForPrintKey] Error storing Summary For Print Key to Session: " + e.getMessage
          )
          None
        }
    }
  }

  def fetchSummaryForPrint()(implicit hc: HeaderCarrier): Future[Option[String]] = {
    logger.info("[SummaryConfirmationService][fetchSummaryForPrintKey]: Reading Summary For Print Key From Session...")

    retry(FHDDSConfig.serviceMaxNoOfAttempts, "Reading Agent Date From Session") {
      sessionRepository.get(getSummaryId)
    }.map(optionFhSession => optionFhSession.flatMap(_.summaryForPrintKey))
      .recover { case e: Throwable =>
        logger.warn(
          "[SummaryConfirmationService][fetchSummaryForPrintKey] Reading Summary For Print Key From Session" + e.getMessage
        )
        None
      }
  }

  def saveWithdrawalReason(reason: WithdrawalReason)(implicit hc: HeaderCarrier): Future[Option[WithdrawalReason]] = {
    logger.info("[SummaryConfirmationService][storeWithdrawalReason]: Storing Withdrawal Reason to Session...")

    val fUpdatedCache = sessionRepository.get(getSummaryId) map {
      case Some(summaryConfirmation) => summaryConfirmation.copy(withdrawalReason = Some(reason))
      case None                      => cleanFHDDSSessionCache(getSummaryId, withdrawalReason = Some(reason))
    }

    fUpdatedCache.flatMap { updatedCache =>
      setSessionCacheDataWithRetry(updatedCache)
        .map(_ => updatedCache.withdrawalReason)
        .recover { case e: Throwable =>
          logger.warn(
            "[SummaryConfirmationService][storeWithdrawalReason] Error storing Withdrawal Reason to Session: " + e.getMessage
          )
          None
        }
    }
  }

  def fetchWithdrawalReason()(implicit hc: HeaderCarrier): Future[Option[WithdrawalReason]] = {
    logger.info("[SummaryConfirmationService][fetchWithdrawalReason]: Reading Withdrawal Reason From Session...")

    retry(FHDDSConfig.serviceMaxNoOfAttempts, "Reading Agent Date From Session") {
      sessionRepository.get(getSummaryId)
    }.map(optionFhSession => optionFhSession.flatMap(_.withdrawalReason))
      .recover { case e: Throwable =>
        logger.warn(
          "[SummaryConfirmationService][fetchWithdrawalReason] Reading Withdrawal Reason From Session" + e.getMessage
        )
        None
      }
  }

  def saveDeregistrationReason(
    reason: DeregistrationReason
  )(implicit hc: HeaderCarrier): Future[Option[DeregistrationReason]] = {
    logger.info("[SummaryConfirmationService][storeAgentData]: Storing Deregistration Reason to Session...")

    val fUpdatedCache = sessionRepository.get(getSummaryId) map {
      case Some(summaryConfirmation) => summaryConfirmation.copy(deregistrationReason = Some(reason))
      case None                      => cleanFHDDSSessionCache(getSummaryId, deregistrationReason = Some(reason))
    }

    fUpdatedCache.flatMap { updatedCache =>
      setSessionCacheDataWithRetry(updatedCache)
        .map(_ => updatedCache.deregistrationReason)
        .recover { case e: Throwable =>
          logger.warn(
            "[SummaryConfirmationService][storeDeregistrationReason]: Error storing Deregistration Reason to Session: " + e.getMessage
          )
          None
        }
    }
  }

  def fetchDeregistrationReason()(implicit hc: HeaderCarrier): Future[Option[DeregistrationReason]] = {
    logger.info("[EISessionLocalService][fetchDeregistrationReason]: Reading Deregistration Reason From Session...")

    retry(FHDDSConfig.serviceMaxNoOfAttempts, "Reading Agent Date From Session") {
      sessionRepository.get(getSummaryId)
    }.map(optionFhSession => optionFhSession.flatMap(_.deregistrationReason))
      .recover { case e: Throwable =>
        logger.warn(
          "[SummaryConfirmationService][fetchDeregistrationReason] Reading Deregistration ReasonFrom Session" + e.getMessage
        )
        None
      }
  }

  private def setSessionCacheDataWithRetry(updatedSession: SummaryConfirmation): Future[Boolean] =
    retry(FHDDSConfig.serviceMaxNoOfAttempts, "Storing FHDDS Session") {
      sessionRepository.set(updatedSession)
    }

}
