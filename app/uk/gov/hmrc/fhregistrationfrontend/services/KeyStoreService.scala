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

import com.google.inject.{ImplementedBy, Singleton}
import uk.gov.hmrc.fhregistrationfrontend.config.KeySessionCache
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class KeyStoreServiceImpl extends KeyStoreService {
  val id = "fhdds-summary-input"

  override def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[String]]= {
    sessionCache.fetchAndGetEntry[String](id)
  }

  override def save(o: String)(implicit hc: HeaderCarrier): Future[CacheMap] = sessionCache.cache(id, o)

  private val sessionCache = KeySessionCache

}

@ImplementedBy(classOf[KeyStoreServiceImpl])
trait KeyStoreService {
  def fetchAndGetEntry()(implicit hc: HeaderCarrier): Future[Option[String]]

  def save(o: String)(implicit hc: HeaderCarrier): Future[CacheMap]
}
