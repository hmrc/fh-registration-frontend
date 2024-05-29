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

package uk.gov.hmrc.fhregistrationfrontend.views.companyOfficers

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig
import uk.gov.hmrc.fhregistrationfrontend.views.ViewSpecHelper
import uk.gov.hmrc.fhregistrationfrontend.views.html.companyOfficers.v2.CompanyOfficersCYA
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}

class CompanyOfficersCYAViewSpec extends ViewSpecHelper {

  val cyaView: CompanyOfficersCYA = views.companyOfficersCYAView

  object Selectors {
    val POST_HEADER_CAPTION = "govuk-body"
    val heading = "govuk-heading-l"
    val button = "govuk-button"
    val summaryList = "govuk-summary-list"
    val summaryRow = "govuk-summary-list__row"
    val summaryValue = "govuk-summary-list__value"
    val form = "form"
  }

  val summaryList: SummaryList =
    SummaryList(
      Seq(
        SummaryListRow(value = Value(content = HtmlContent("testing")))
      ))

  val backLink = "http://test.com"
  val call = Call("GET", "/foo")
  val html = cyaView(backLink, summaryList, call)(request, Messages)
  val document = doc(html)

  "cyaView" should {
    "have the expected title" in {
      document.title() mustEqual "Check your answers - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK"
    }
    "should have the expected heading" in {
      document.getElementsByTag("h1").text() mustEqual "this is for: Company officers Check your answers"
    }
    "contain the correct button" in {
      document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
    }
    "contain the correct summary lists" in {
      document
        .getElementsByClass(Selectors.summaryValue)
        .first()
        .text() mustBe "testing"
    }
    testBackLink(document, backLink)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
