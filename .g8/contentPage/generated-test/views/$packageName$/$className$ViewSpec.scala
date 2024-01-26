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

package views.$packageName$

import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.$packageName$.$className$View
import views.ViewSpecHelper

class $className$ViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[$className$View]
  implicit val request: Request[_] = FakeRequest()

  object Selectors {
    val heading = "govuk-heading-l"
  }

  "View" - {
    val html = view()(request, messages(application))
    val document = doc(html)
    "should contain the expected title" in {
      document.title() must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("$packageName$.$className;format="decap"$" + ".heading")
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

}
