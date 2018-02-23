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

import com.google.inject.ImplementedBy
import org.joda.time.DateTime
import play.api.libs.json
import play.api.libs.json.{Reads, Writes}
import uk.gov.hmrc.fhregistrationfrontend.cache.ShortLivedCache
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.ShortLivedCache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Save4LaterKeys {
  val businessRegistrationDetailsKey = "businessRegistrationDetails"
  val businessTypeKey = "businessType"
  val userLastTimeSavedKey = "userLastTimeSaved"
}

@ImplementedBy(classOf[Save4LaterServiceImpl])
trait Save4LaterService {

  import Save4LaterKeys._

  implicit val dateReads: Reads[DateTime] = Reads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val dateWrites: Writes[DateTime] = Writes.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")

  val shortLivedCache: ShortLivedCache

  def saveBusinessType(userId: String, businessType: BusinessType)(implicit hc: HeaderCarrier) = {
    saveData4Later(userId, businessTypeKey, businessType)
  }

  def fetchBusinessType(userId: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {
    fetchData4Later[String](userId, businessTypeKey)
  }

  def saveBusinessRegistrationDetails(userId: String, brd: BusinessRegistrationDetails)(implicit hc: HeaderCarrier) = {
    saveData4Later(userId, businessRegistrationDetailsKey, brd)
  }

  def fetchBusinessRegistrationDetails(userId: String)(implicit hc: HeaderCarrier) = {
    fetchData4Later[BusinessRegistrationDetails](userId, businessRegistrationDetailsKey)
  }

  def fetchLastUpdateTime(userId: String)(implicit hc: HeaderCarrier): Future[Option[DateTime]] = {
    fetchData4Later[DateTime](userId, userLastTimeSavedKey)
  }

  def removeUserData(userId: String)(implicit hc: HeaderCarrier) = {
    shortLivedCache.remove(userId)
  }

  def saveData4Later[T](id: String, formId: String, data: T)(implicit hc: HeaderCarrier, formats: json.Format[T]): Future[Option[T]] = {
    val lastTimeUserSaved: DateTime = new DateTime()
    shortLivedCache.cache(id, userLastTimeSavedKey, lastTimeUserSaved).flatMap { _ ⇒
      shortLivedCache.cache(id, formId, data) map {
        data ⇒ data.getEntry[T](formId)
      }
    }
  }

  def fetchData4Later[T](utr: String, formId: String)(implicit hc: HeaderCarrier, formats: json.Format[T]): Future[Option[T]] =
    shortLivedCache.fetchAndGetEntry[T](utr, formId)

}

class Save4LaterServiceImpl extends Save4LaterService {
  override val shortLivedCache = ShortLivedCache
}
