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

package uk.gov.hmrc.fhregistrationfrontend.services

import javax.inject.{Inject, Singleton}

import play.api.libs.json
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.models.des
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.ShortLivedCache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Save4LaterKeys {
  val businessRegistrationDetailsKey = "businessRegistrationDetails"
  val businessTypeKey = "businessType"
  val userLastTimeSavedKey = "userLastTimeSaved"
  val isAmendmentKey = "isAmendment"
  val journeyTypeKey = "journeyType"
  val verifiedEmailKey = "verifiedEmail"
  val v1ContactEmailKey = "v1ContactEmail"
  val pendingEmailKey = "pendingEmail"
  def displayKeyForPage(pageId: String) =  s"display_$pageId"
  val displayDesDeclarationKey =  s"display_des_declaration"
}

@Singleton
class Save4LaterService @Inject() (
  shortLivedCache: ShortLivedCache
) {

  import Save4LaterKeys._

  def fetch(userId: String)(implicit hc: HeaderCarrier) = shortLivedCache.fetch(userId)

  def saveBusinessType(userId: String, businessType: BusinessType)(implicit hc: HeaderCarrier) = {
    saveDraftData4Later(userId, businessTypeKey, businessType)
  }

  def fetchBusinessType(userId: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    fetchData4Later[String](userId, businessTypeKey)
  }

  def saveVerifiedEmail(userId: String, email: String)(implicit hc: HeaderCarrier) = {
    saveDraftData4Later(userId, verifiedEmailKey, email)
  }

  def fetchVerifiedEmail(userId: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    fetchData4Later[String](userId, verifiedEmailKey)
  }


  def savePendingEmail(userId: String, email: String)(implicit hc: HeaderCarrier) = {
    saveDraftData4Later(userId, pendingEmailKey, email)
  }

  def saveV1ContactEmail(userId: String, email: String)(implicit hc: HeaderCarrier) = {
    saveDraftData4Later(userId, v1ContactEmailKey, email)
  }

  def deletePendingEmail(userId: String)(implicit hc: HeaderCarrier) = {
    savePendingEmail(userId, "")
  }

  def fetchPendingEmail(userId: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    fetchData4Later[String](userId, pendingEmailKey).map { _.filterNot( _.isEmpty) }
  }


  def saveBusinessRegistrationDetails(userId: String, brd: BusinessRegistrationDetails)(implicit hc: HeaderCarrier) = {
    saveDraftData4Later(userId, businessRegistrationDetailsKey, brd)
  }

  def fetchBusinessRegistrationDetails(userId: String)(implicit hc: HeaderCarrier) = {
    fetchData4Later[BusinessRegistrationDetails](userId, businessRegistrationDetailsKey)
  }

  def fetchLastUpdateTime(userId: String)(implicit hc: HeaderCarrier): Future[Option[Long]] = {
    fetchData4Later[Long](userId, userLastTimeSavedKey)
  }

  def removeUserData(userId: String)(implicit hc: HeaderCarrier): Future[Any] = {
    shortLivedCache.remove(userId)
  }

  def saveDraftData4Later[T](userId: String, formId: String, data: T)(implicit hc: HeaderCarrier, formats: json.Format[T]): Future[Option[T]] = {
    val lastTimeUserSaved = System.currentTimeMillis()
    shortLivedCache.cache(userId, userLastTimeSavedKey, lastTimeUserSaved).flatMap { _ ⇒
      shortLivedCache.cache(userId, formId, data) map {
        data ⇒ data.getEntry[T](formId)
      }
    }
  }

  def saveJourneyType(userId: String, journeyType: JourneyType)(implicit hc: HeaderCarrier) = {
    shortLivedCache.cache(userId, journeyTypeKey, journeyType) map {
      data ⇒ data.getEntry[JourneyType](journeyTypeKey)
    }
  }


  def saveDisplayData4Later[T](userId: String, formId: String, data: T)(implicit hc: HeaderCarrier, formats: json.Format[T]): Future[Option[T]] = {
    val key = displayKeyForPage(formId)
    shortLivedCache.cache(userId, key, data) map {
      data ⇒ data.getEntry[T](key)
    }
  }

  def saveDisplayDeclaration(userId: String, declaration: des.Declaration)(implicit hc: HeaderCarrier) = {
    shortLivedCache.cache(userId, displayDesDeclarationKey, declaration) map {
      data ⇒ data.getEntry[des.Declaration](displayDesDeclarationKey)
    }
  }

  def fetchData4Later[T](utr: String, formId: String)(implicit hc: HeaderCarrier, formats: json.Format[T]): Future[Option[T]] =
    shortLivedCache.fetchAndGetEntry[T](utr, formId)

}

