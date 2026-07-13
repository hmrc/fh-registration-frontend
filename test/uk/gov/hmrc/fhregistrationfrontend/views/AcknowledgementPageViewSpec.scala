/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.views

import java.util.Date

import org.scalatest.matchers.must.Matchers.mustBe
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.New

class AcknowledgementPageViewSpec extends ViewSpecHelper {

  private val acknowledgementPage = views.acknowledgement_page

  "acknowledgement_page" should {

    "render the updated print link markup" in {
      val html = acknowledgementPage(
        new Date(),
        "user@test.com",
        Html("<p>summary</p>"),
        New
      )(using request, Messages, appConfig)

      val document = doc(html)

      val printLink = document.select("a[data-module=hmrc-print-link]").first()

      printLink.hasClass("hmrc-!-js-visible") mustBe true
      printLink.attr("href") mustBe "#"

      document.html().contains("icon-file-download") mustBe false
      document.html().contains("js-show") mustBe false
    }
  }
}
