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

import play.api.test.FakeRequest
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{CachedJourneyState, JourneyPages, JourneyType, Journeys}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.teststubs.CacheMapBuilder

trait JourneyRequestBuilder {

  this: ActionSpecBase ⇒

  def journeyRequest(journeyPages: JourneyPages = new JourneyPages(Journeys.partnershipPages)) = {
    val userRequest = new UserRequest(testUserId, None, None, FakeRequest())
    new JourneyRequest(
      CacheMapBuilder(testUserId).cacheMap,
      userRequest,
      FormTestData.someBpr,
      BusinessType.Partnership,
      ggEmail,
      JourneyType.Amendment,
      journeyPages,
      new CachedJourneyState(journeyPages)
    )
  }


}
