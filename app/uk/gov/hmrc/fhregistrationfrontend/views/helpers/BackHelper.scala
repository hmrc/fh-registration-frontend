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

package uk.gov.hmrc.fhregistrationfrontend.views.helpers

import uk.gov.hmrc.fhregistrationfrontend.forms.navigation._

object BackHelper {

  private def call(navigation: Navigation) = navigation.back.map {
    case FormPage(pageId, None) => uk.gov.hmrc.fhregistrationfrontend.controllers.routes.FormPageController.load(pageId)
    case FormPage(pageId, Some(subsection)) =>
      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.FormPageController.loadWithSection(pageId, subsection)
    case _ => uk.gov.hmrc.fhregistrationfrontend.controllers.routes.SummaryController.summary

  }

  def getUrl(navigation: Navigation): Option[String] = call(navigation).map(_.url).headOption

}
