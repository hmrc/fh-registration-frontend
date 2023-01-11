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

package uk.gov.hmrc.fhregistrationfrontend.forms.journey
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page.AnyPage
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.{FormPage, Navigation}

class LinearJourney(val journeyPages: JourneyPages) extends JourneyNavigation {

  val pages = journeyPages.pages

  override def next[_](page: AnyPage): Option[AnyPage] =
    pages dropWhile (_.id != page.id) match {
      case _ :: next :: rest => Some(next)
      case _                 => None
    }

  override def previous(page: AnyPage): Option[AnyPage] = {
    val pageWithPrevSubsection = page.previousSubsection map (subsection => page withSubsection Some(subsection))

    pageWithPrevSubsection orElse {
      pages.takeWhile(_.id != page.id).lastOption map { prevPage =>
        prevPage withSubsection prevPage.lastSection
      }
    }
  }

  override def navigation(lastUpdateTime: Long, page: AnyPage): Navigation =
    Navigation(lastUpdateTime, back = previous(page) map { prevPage =>
      FormPage(prevPage.id, prevPage.section)
    })
}
