/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.teststubs

import play.api.libs.json.{JsValue, Reads, Writes}
import uk.gov.hmrc.crypto.CompositeSymmetricCrypto
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.http.cache.client.{CacheMap, ShortLivedCache, ShortLivedHttpCaching}

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class InMemoryShortLivedCache(userId: String) extends ShortLivedCache {

  val cache = mutable.Map[String, JsValue]()

  override implicit val crypto: CompositeSymmetricCrypto = null
  override def shortLiveCache: ShortLivedHttpCaching = null

  override def cache[A](cacheId: String, formId: String, body: A)(implicit hc: HeaderCarrier, wts: Writes[A], executionContext: ExecutionContext): Future[CacheMap] = {
    require(userId == cacheId)
    cache.put(formId, wts writes body)
    Future successful CacheMap(cacheId, cache.toMap)
  }

  override def fetch(cacheId: String)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[Option[CacheMap]] = {
    require(userId == cacheId)
    Future successful Some(CacheMap(cacheId, cache.toMap))
  }

  override def fetchAndGetEntry[T](cacheId: String, key: String)(implicit hc: HeaderCarrier, rds: Reads[T], executionContext: ExecutionContext): Future[Option[T]] = {
    require(userId == cacheId)
    Future successful (cache get key map(_.as[T]))
  }

  override def remove(cacheId: String)(implicit hc: HeaderCarrier, executionContext: ExecutionContext): Future[HttpResponse] = {
    require(userId == cacheId)
    cache.clear()
    Future successful HttpResponse(200)
  }
}
