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

import javax.inject.{Inject, Singleton}
import models.UserAnswers
import play.api.libs.json
import play.api.libs.json.{JsString, JsValue}
import uk.gov.hmrc.crypto.{Crypted, CryptoWithKeysFromConfig}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.des
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.ShortLivedCache

import scala.concurrent.{ExecutionContext, Future}

object Save4LaterKeys {
  val businessRegistrationDetailsKey = "businessRegistrationDetails"
  val businessTypeKey = "businessType"
  val userLastTimeSavedKey = "userLastTimeSaved"
  val isAmendmentKey = "isAmendment"
  val journeyTypeKey = "journeyType"
  val verifiedEmailKey = "verifiedEmail"
  val v1ContactEmailKey = "v1ContactEmail"
  val pendingEmailKey = "pendingEmail"
  def displayKeyForPage(pageId: String) = s"display_$pageId"
  val displayDesDeclarationKey = s"display_des_declaration"
}

@Singleton
class Save4LaterService @Inject() (
  shortLivedCache: ShortLivedCache,
  sessionRepository: SessionRepository,
  appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  import Save4LaterKeys._
  // TODO: remove debug logging after QA testing
  private val logger = play.api.Logger(getClass)
  private lazy val save4LaterCrypto =
    new CryptoWithKeysFromConfig("json.encryption", appConfig.getConfiguration.underlying)

  def fetch(userId: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] =
    if (appConfig.isNewSessionRepositoryCacheEnabled) {
      logger.info(s"[Save4LaterService.fetch] flag ON, checking session repo for $userId")
      sessionRepository.get(userId).flatMap {
        case some @ Some(_) =>
          logger.info(s"[Save4LaterService.fetch] session repo hit for $userId")
          Future.successful(some)
        case None =>
          logger.info(s"[Save4LaterService.fetch] session repo miss for $userId; falling back to ShortLivedCache")
          shortLivedCache.fetch(userId).flatMap {
            case Some(cacheMap) =>
              val userAnswers = UserAnswers(userId, decryptSave4LaterData(cacheMap.data))
              sessionRepository
                .set(userAnswers)
                .map(_ => Option(userAnswers))
                .recover { case e =>
                  logger
                    .warn(s"[Save4LaterService.fetch] session repo write failed for $userId; returning old cache", e)
                  Option(userAnswers)
                }
            case None =>
              logger.info(s"[Save4LaterService.fetch] no data in ShortLivedCache for $userId")
              Future.successful(None)
          }
      }
    } else {
      logger.info(s"[Save4LaterService.fetch] flag OFF, using ShortLivedCache for $userId")
      shortLivedCache.fetch(userId).map(_.map(cacheMap => UserAnswers(userId, decryptSave4LaterData(cacheMap.data))))
    }

  def saveBusinessType(userId: String, businessType: BusinessType)(implicit hc: HeaderCarrier) =
    saveDraftData4Later(userId, businessTypeKey, businessType)

  def fetchBusinessType(userId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    fetchData4Later[String](userId, businessTypeKey)

  def saveVerifiedEmail(userId: String, email: String)(implicit hc: HeaderCarrier) =
    saveDraftData4Later(userId, verifiedEmailKey, email)

  def fetchVerifiedEmail(userId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    fetchData4Later[String](userId, verifiedEmailKey)

  def savePendingEmail(userId: String, email: String)(implicit hc: HeaderCarrier) =
    saveDraftData4Later(userId, pendingEmailKey, email)

  def saveV1ContactEmail(userId: String, email: String)(implicit hc: HeaderCarrier) =
    saveDraftData4Later(userId, v1ContactEmailKey, email)

  def fetchV1ContactEmail(userId: String)(implicit hc: HeaderCarrier) =
    fetchData4Later[String](userId, v1ContactEmailKey)

  def deletePendingEmail(userId: String)(implicit hc: HeaderCarrier) =
    savePendingEmail(userId, "")

  def fetchPendingEmail(userId: String)(implicit hc: HeaderCarrier): Future[Option[String]] =
    fetchData4Later[String](userId, pendingEmailKey).map(_.filterNot(_.isEmpty))

  def saveBusinessRegistrationDetails(userId: String, brd: BusinessRegistrationDetails)(implicit hc: HeaderCarrier) =
    saveDraftData4Later(userId, businessRegistrationDetailsKey, brd)

  def fetchBusinessRegistrationDetails(userId: String)(implicit hc: HeaderCarrier) =
    fetchData4Later[BusinessRegistrationDetails](userId, businessRegistrationDetailsKey)

  def fetchLastUpdateTime(userId: String)(implicit hc: HeaderCarrier): Future[Option[Long]] =
    fetchData4Later[Long](userId, userLastTimeSavedKey)

  def removeUserData(userId: String)(implicit hc: HeaderCarrier): Future[Any] =
    sessionRepository
      .clear(userId)
      .recover { case e =>
        logger.warn(s"[Save4LaterService.removeUserData] session repo clear failed for $userId; removing old cache", e)
        false
      }
      .flatMap(_ => shortLivedCache.remove(userId))

  def saveDraftData4Later[T](userId: String, formId: String, data: T)(implicit
    hc: HeaderCarrier,
    formats: json.Format[T]
  ): Future[Option[T]] = {
    val lastTimeUserSaved = System.currentTimeMillis()
    val persistToOldCache = () =>
      shortLivedCache.cache(userId, userLastTimeSavedKey, lastTimeUserSaved).flatMap { _ =>
        shortLivedCache.cache(userId, formId, data) map { data =>
          data.getEntry[T](formId)
        }
      }

    if (appConfig.isNewSessionRepositoryCacheEnabled) {
      logger.info(s"[Save4LaterService.saveDraftData4Later] dual-write for $userId, key=$formId")
      updateSessionRepository(userId, formId, data, Some(lastTimeUserSaved))
        .recover { case e =>
          logger.warn(
            s"[Save4LaterService.saveDraftData4Later] session repo write failed for $userId, key=$formId; continuing with old cache",
            e
          )
          false
        }
        .flatMap(_ => persistToOldCache())
    } else {
      logger.info(s"[Save4LaterService.saveDraftData4Later] old cache only for $userId, key=$formId")
      persistToOldCache()
    }
  }

  def saveJourneyType(userId: String, journeyType: JourneyType)(implicit hc: HeaderCarrier) =
    saveToBothCaches(userId, journeyTypeKey, journeyType)

  def saveDisplayData4Later[T](userId: String, formId: String, data: T)(implicit
    hc: HeaderCarrier,
    formats: json.Format[T]
  ): Future[Option[T]] = {
    val key = displayKeyForPage(formId)
    saveToBothCaches(userId, key, data)
  }

  def saveDisplayDeclaration(userId: String, declaration: des.Declaration)(implicit hc: HeaderCarrier) =
    saveToBothCaches(userId, displayDesDeclarationKey, declaration)

  def fetchData4Later[T](utr: String, formId: String)(implicit
    hc: HeaderCarrier,
    formats: json.Format[T]
  ): Future[Option[T]] =
    fetch(utr).map(_.flatMap(_.getEntry[T](formId)))

  private def updateSessionRepository[T](userId: String, key: String, value: T, lastSavedTime: Option[Long])(implicit
    formats: json.Format[T]
  ): Future[Boolean] =
    sessionRepository.setEntries(
      userId,
      Map(key -> json.Json.toJson(value)) ++ lastSavedTime.map(time => userLastTimeSavedKey -> json.Json.toJson(time))
    )

  private def saveToBothCaches[T](userId: String, formId: String, data: T)(implicit
    hc: HeaderCarrier,
    formats: json.Format[T]
  ): Future[Option[T]] = {
    val persistToOldCache = () =>
      shortLivedCache.cache(userId, formId, data) map { data =>
        data.getEntry[T](formId)
      }

    if (appConfig.isNewSessionRepositoryCacheEnabled) {
      logger.info(s"[Save4LaterService.saveToBothCaches] dual-write for $userId, key=$formId")
      updateSessionRepository(userId, formId, data, None)
        .recover { case e =>
          logger.warn(
            s"[Save4LaterService.saveToBothCaches] session repo write failed for $userId, key=$formId; continuing with old cache",
            e
          )
          false
        }
        .flatMap(_ => persistToOldCache())
    } else {
      logger.info(s"[Save4LaterService.saveToBothCaches] old cache only for $userId, key=$formId")
      persistToOldCache()
    }
  }

  private def decryptSave4LaterData(data: Map[String, JsValue]): Map[String, JsValue] =
    data.map { case (key, value) =>
      key -> decryptSave4LaterValue(value)
    }

  private def decryptSave4LaterValue(value: JsValue): JsValue =
    value match {
      case JsString(encrypted) =>
        val decrypted =
          try Some(save4LaterCrypto.decrypt(Crypted(encrypted)).value)
          catch { case _: Throwable => None }
        decrypted.map(parseJsonOrString).getOrElse(JsString(encrypted))
      case other => other
    }

  private def parseJsonOrString(value: String): JsValue =
    try
      json.Json.parse(value)
    catch {
      case _: Throwable => JsString(value)
    }
}
