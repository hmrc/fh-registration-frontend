package uk.gov.hmrc.fhregistrationfrontend.services
import services.helper.Retryable
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.DeregistrationReason
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.WithdrawalReason
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future


class SummaryConfirmationService @Inject()(
  fhSessionKeystoreService: KeyStoreService,
  fhSessionLocalService: SummaryConfirmationLocalService,
  fhConfig: FrontendAppConfig) extends Retryable{

  private def featureSwitchCheck[A](expr1: => Future[A], expr2: => Future[A]): Future[A] = {
    if (fhConfig.isMongoDBCacheEnabled) expr1 else expr2
  }

  def saveSummaryForPrint(o: String)(implicit hc: HeaderCarrier): Future[Any] = {
    featureSwitchCheck(
      fhSessionKeystoreService.saveSummaryForPrint(o),
      fhSessionLocalService.saveSummaryForPrint(o)
    )
  }


  def fetchSummaryForPrint()(implicit hc: HeaderCarrier): Future[Option[String]] = {
    featureSwitchCheck(
      fhSessionKeystoreService.fetchSummaryForPrint(),
      fhSessionLocalService.fetchSummaryForPrint()
    )
  }

  def saveWithdrawalReason(reason: WithdrawalReason)(implicit hc: HeaderCarrier): Future[Any] = {
    featureSwitchCheck(
      fhSessionKeystoreService.saveWithdrawalReason(reason),
      fhSessionLocalService.saveWithdrawalReason(reason)
    )
  }


  def fetchWithdrawalReason()(implicit hc: HeaderCarrier): Future[Option[WithdrawalReason]] = {
    featureSwitchCheck(
      fhSessionKeystoreService.fetchWithdrawalReason(),
      fhSessionLocalService.fetchWithdrawalReason()
    )
  }

  def saveDeregistrationReason(
                                reason: DeregistrationReason
                              )(implicit hc: HeaderCarrier): Future[Any] = {
    featureSwitchCheck(
      fhSessionKeystoreService.saveDeregistrationReason(reason),
      fhSessionLocalService.saveDeregistrationReason(reason)
    )

  }

  def fetchDeregistrationReason()(implicit hc: HeaderCarrier): Future[Option[DeregistrationReason]] = {
    featureSwitchCheck(
      fhSessionKeystoreService.fetchDeregistrationReason(),
      fhSessionLocalService.fetchDeregistrationReason()
    )
  }

  }