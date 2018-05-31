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

import cats.data.EitherT
import cats.implicits._
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.FhddsStatus.{Approved, ApprovedWithConditions}
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService

import scala.concurrent.Future


class StartVariationAction(fhddsConnector: FhddsConnector)(implicit val save4LaterService: Save4LaterService, errorHandler: ErrorHandler)
  extends ActionRefiner[UserRequest, StartUpdateRequest]
    with FrontendAction
    with ActionFunctions {

  override protected def refine[A](request: UserRequest[A]): Future[Either[Result, StartUpdateRequest[A]]] = {

    implicit val r = request

    val whenRegistered = request.registrationNumber.map {
      registrationNumber ⇒
        val result = for {
          _ ← EitherT(checkIsApproved(registrationNumber))
          cacheMap ← EitherT(loadCacheMap)
          journeyType = loadJourneyType(cacheMap)
        } yield {
          new StartUpdateRequest[A](registrationNumber, Some(journeyType), request)
        }
        result.value
    }


    whenRegistered getOrElse Future.successful(Left(errorHandler.errorResultsPages(Results.BadRequest)))
  }

  private def checkIsApproved(registrationNumber: String)(implicit request: UserRequest[_]): Future[Either[Result, Boolean]] = {
    fhddsConnector.getStatus(registrationNumber) map {
      case Approved | ApprovedWithConditions ⇒ Right(true)
      case _                                 ⇒ Left(errorHandler.errorResultsPages(Results.BadRequest))
    }
  }

}
