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

package uk.gov.hmrc.fhregistrationfrontend.teststubs

import org.mockito.ArgumentMatchers.{any, same}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.services.{Save4LaterKeys, Save4LaterService}
import uk.gov.hmrc.http.cache.client.CacheMap
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

import scala.concurrent.Future

trait Save4LaterMocks extends MockitoSugar with UserTestData {
  this: UnitSpec =>

  val mockSave4Later = mock[Save4LaterService]
  private val ok = Future successful None

  def setupSave4Later(userId: String = testUserId): Unit =
    setupSave4LaterFrom(CacheMapBuilder(userId).cacheMap, userId)

  def setupSave4LaterFrom(cacheMap: CacheMap, userId: String = testUserId): Unit = {
    when(mockSave4Later.fetch(same(userId))(any())).thenReturn(Future.successful(Some(cacheMap)))

    when(mockSave4Later.fetchBusinessRegistrationDetails(same(userId))(any()))
      .thenReturn(
        Future successful cacheMap.getEntry[BusinessRegistrationDetails](Save4LaterKeys.businessRegistrationDetailsKey)
      )

    when(mockSave4Later.fetchBusinessType(same(userId))(any()))
      .thenReturn(Future successful cacheMap.getEntry[String](Save4LaterKeys.businessTypeKey))

    when(mockSave4Later.fetchLastUpdateTime(same(userId))(any()))
      .thenReturn(Future successful cacheMap.getEntry[Long](Save4LaterKeys.userLastTimeSavedKey))

    when(mockSave4Later.saveBusinessRegistrationDetails(same(userId), any())(any()))
      .thenReturn(ok)

    when(mockSave4Later.removeUserData(same(userId))(any())).thenReturn(ok)

    when(mockSave4Later.saveBusinessType(same(userId), any())(any())).thenReturn(ok)
    when(mockSave4Later.saveDraftData4Later(same(userId), any(), any())(any(), any())).thenReturn(ok)
  }

}
