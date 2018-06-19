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

package uk.gov.hmrc.fhregistrationfrontend.actions

import org.mockito.ArgumentMatchers.{any, same}
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

trait Save4LaterMocks extends MockitoSugar {
  this: ActionSpecBase ⇒

  val mockSave4Later = mock[Save4LaterService]

  def setupSave4Later(cacheMap: CacheMap) =
    when(mockSave4Later.fetch(same(testUserId))(any())) thenReturn Future.successful(Some(cacheMap))


}
