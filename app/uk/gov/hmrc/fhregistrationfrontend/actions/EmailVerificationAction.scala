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
import play.api.mvc.{ActionRefiner, Result, WrappedRequest}
import uk.gov.hmrc.fhregistrationfrontend.config.ErrorHandler
import uk.gov.hmrc.fhregistrationfrontend.forms.models.ContactPerson
import uk.gov.hmrc.fhregistrationfrontend.services.{Save4LaterKeys, Save4LaterService}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future

class EmailVerificationRequest[A](
  val verifiedEmail: Option[String],
  val pendingEmail: Option[String],
  val candidateEmail: Option[String],
  request: UserRequest[A]) extends WrappedRequest(request) {

  def userId = request.userId
}

class EmailVerificationAction(implicit val save4LaterService: Save4LaterService, errorHandler: ErrorHandler)
  extends ActionRefiner[UserRequest, EmailVerificationRequest]
    with FrontendAction
    with ActionFunctions
{

  override protected def refine[A](request: UserRequest[A]): Future[Either[Result, EmailVerificationRequest[A]]] = {
    implicit val r = request
    val result = for {
      cacheMap ← EitherT(loadCacheMap)
      verifiedEmail = cacheMap.getEntry[String](Save4LaterKeys.verifiedEmailKey)
      pendingEmail = getPendingEmail(cacheMap)
      contactPersonEmail = getContactPersonEmail(cacheMap)
      contactPersonV1Email = getContactPersonEmailV1(cacheMap)

    } yield {

      val candidateEmail =
        verifiedEmail orElse pendingEmail orElse contactPersonEmail orElse contactPersonV1Email orElse request.ggEmail

      new EmailVerificationRequest[A](
        verifiedEmail,
        pendingEmail,
        candidateEmail,
        request
      )
    }
    result.value
  }

  private def getPendingEmail(cacheMap: CacheMap) = cacheMap
    .getEntry[String](Save4LaterKeys.pendingEmailKey)
    .filterNot(_.isEmpty)


  private def getContactPersonEmail(cacheMap: CacheMap) =
    cacheMap
      .getEntry[ContactPerson]("contactPerson")
      .flatMap(_.emailAddress)

  private def getContactPersonEmailV1(cacheMap: CacheMap) =
    cacheMap
      .getEntry[String](Save4LaterKeys.v1ContactEmailKey)

}
