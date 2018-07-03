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
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation

class JourneyPages(val pages: Seq[AnyPage]) {

  def get[T](pageId: String): Option[Page[T]] = {
    pages find ( _.id == pageId) map (_.asInstanceOf[Page[T]])
  }
}

trait JourneyNavigation {

  def next[_](page: AnyPage): Option[AnyPage]

  def previous(page: AnyPage): Option[AnyPage]

  def navigation(lastUpdateTime: Long, page: AnyPage): Navigation

}

trait JourneyState {
  def isComplete: Boolean
  def isAccessible(pageId: String): Boolean

  def nextPageToComplete(): Option[String]
  def isPageComplete(page: AnyPage): Boolean

  def get[T](pageId: String): Option[Page[T]]
  def lastEditedPage: Option[AnyPage]
}