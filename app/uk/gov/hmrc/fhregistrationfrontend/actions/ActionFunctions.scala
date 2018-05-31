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

import play.api.Logger
import play.api.mvc.{Result, Results}
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.services.{Save4LaterKeys, Save4LaterService}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

trait ActionFunctions {
  this: FrontendAction ⇒

  def loadCacheMap(implicit save4LaterService: Save4LaterService,  errorHandler: ErrorHandler, request: UserRequest[_]): Future[Either[Result, CacheMap]] = {
    save4LaterService.fetch(request.userId) map {
      case Some(cacheMap) ⇒ Right(cacheMap)
      case None ⇒ Right(new CacheMap(request.userId, Map.empty))

    } recover { case t ⇒
      Logger.error(s"Could not access shortLivedCache", t)
      Left(errorHandler.errorResultsPages(Results.BadGateway))
    }
  }

  def loadJourneyType(cacheMap: CacheMap): JourneyType = {
    cacheMap
      .getEntry[JourneyType](Save4LaterKeys.journeyTypeKey)
      .orElse(
        cacheMap
          .getEntry[Boolean](Save4LaterKeys.isAmendmentKey)
          .collect {case true ⇒ JourneyType.Amendment})
      .getOrElse(JourneyType.New)
  }

}
