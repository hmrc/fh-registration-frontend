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

import play.api.mvc.{ActionRefiner, Result, WrappedRequest}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{Journey, LinearJourney, Page}

import scala.concurrent.Future


class PageRequest[A](
  val journey: Journey,
  p: Page[_],
  request: UserRequest[A]) extends WrappedRequest[A](request)
{

  def page[T] = p.asInstanceOf[Page[T]]
  def userId = request.userId
}

object PageAction {
  def apply(sectionId: String) = (UserAction andThen new PageAction(sectionId))
}

class PageAction[T](sectionId: String) extends ActionRefiner[UserRequest, PageRequest]
  with FrontendAction
{

  def refine[A](input: UserRequest[A]): Future[Either[Result, PageRequest[A]]] = {
    val journey = new LinearJourney
    Future successful {
      journey.get[T](sectionId) match {
        case Some(page) ⇒
          //TODO check if allowed on this page and if not Left(Unauthorized)
          Right(new PageRequest[A](journey, page, input))
        case None ⇒ Left(NotFound)
      }
    }
  }

}
