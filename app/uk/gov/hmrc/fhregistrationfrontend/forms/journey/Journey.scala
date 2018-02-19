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

import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation

class JourneyPages(val pages: Seq[Page[_]]) {

  def get[T](pageId: String): Option[Page[T]] = {
    pages find ( _.id == pageId) map (_.asInstanceOf[Page[T]])
  }
}

trait JourneyNavigation {

  def next[_](pageId: String): Option[Page[_]]

  def previous(pageId: String): Option[Page[_]]

  def navigation(pageId: String): Navigation

}

trait JourneyState {
  def isComplete: Boolean
  def isAccessible(pageId: String): Boolean

  def nextPageToComplete(): Option[String]
  def isPageComplete(page: Page[_]): Boolean
}