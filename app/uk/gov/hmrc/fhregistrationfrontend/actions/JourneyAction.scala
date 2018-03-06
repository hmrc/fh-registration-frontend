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

import org.joda.time.DateTime
import play.api.mvc.Result
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{JourneyPages, JourneyState, Journeys}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterKeys.businessRegistrationDetailsKey
import uk.gov.hmrc.fhregistrationfrontend.services.{Save4LaterKeys, Save4LaterService}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

trait JourneyAction extends FrontendAction {
  implicit val save4LaterService: Save4LaterService

  def loadCacheMap(implicit request: UserRequest[_]): Future[Either[Result, CacheMap]] = {
    save4LaterService.shortLivedCache.fetch(request.userId) map {
      case Some(cacheMap) ⇒ Right(cacheMap)
      case None ⇒ Left(NotFound)
    } recover { case t ⇒
      Left(BadGateway)
    }
  }

  def findBpr(cacheMap: CacheMap): Either[Result, BusinessRegistrationDetails] = {
    cacheMap.getEntry[BusinessRegistrationDetails](businessRegistrationDetailsKey) match {
      case Some(bpr) ⇒ Right(bpr)
      case None ⇒ Left(NotFound("Not found: bpr"))
    }
  }

  def getBusinessType(cacheMap: CacheMap): Either[Result, BusinessType] = {
    cacheMap.getEntry[BusinessType](Save4LaterKeys.businessTypeKey) match {
      case Some(bt) ⇒ Right(bt)
      case None ⇒ Left(NotFound("Not found: business type"))
    }
  }

  def lastUpdateTimestamp(cacheMap: CacheMap) = {
    cacheMap.getEntry[Long](Save4LaterKeys.userLastTimeSavedKey) getOrElse 0L
  }

  def getJourneyPages(cacheMap: CacheMap): Either[Result, JourneyPages] = {
    getBusinessType(cacheMap).right flatMap {
      _ match {
        case BusinessType.CorporateBody ⇒ Right(Journeys.limitedCompanyPages)
        case BusinessType.SoleTrader    ⇒ Right(Journeys.soleTraderPages)
        case BusinessType.Partnership   ⇒ Right(Journeys.partnershipPages)
        case _                          ⇒ Left(NotFound("Not found: wrong business type"))
      }
    }
  }

  def loadJourneyState(journeyPages: JourneyPages, cacheMap: CacheMap): JourneyState = {
    Journeys.journeyState(journeyPages, cacheMap)
  }
}
