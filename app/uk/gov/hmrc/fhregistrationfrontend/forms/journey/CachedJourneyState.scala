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

package uk.gov.hmrc.fhregistrationfrontend.forms.journey

import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page.AnyPage
import uk.gov.hmrc.http.cache.client.CacheMap

class CachedJourneyState(journeyPages: JourneyPages) extends JourneyState {

  val pages = journeyPages.pages

  override def isComplete: Boolean = pages forall isPageComplete

  override def isAccessible(pageId: String): Boolean = pages indexWhere (_.id == pageId) match {
    case -1 ⇒ false
    case index ⇒ pages take index forall isPageComplete
  }

  override def nextPageToComplete(): Option[String] = pages find { p ⇒ !isPageComplete(p)} map (_.id)


  override def isPageComplete(page: AnyPage) = page.pageStatus == Completed

  def get[T](pageId: String): Option[Page[T]] = {
    pages find ( _.id == pageId) map (_.asInstanceOf[Page[T]])
  }

  override def lastEditedPage: Option[AnyPage] = {
    val firstNotCompletedIndex = pages indexWhere ( _.pageStatus != Completed)

    if (firstNotCompletedIndex < 0)
      None
    else
      pages(firstNotCompletedIndex).pageStatus match {
        case InProgress ⇒ Some(pages(firstNotCompletedIndex))
        case NotStarted ⇒
          if (firstNotCompletedIndex == 0)
            None
          else
            Some(pages(firstNotCompletedIndex - 1))
        case Completed ⇒ throw new IllegalArgumentException("must be != Completed")
      }

  }

}
