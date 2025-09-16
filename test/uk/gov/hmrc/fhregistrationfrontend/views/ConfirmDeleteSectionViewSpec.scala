/*
 * Copyright 2024 HM Revenue & Customs
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

import org.scalatest.matchers.must.Matchers.{mustBe, mustEqual}
import uk.gov.hmrc.fhregistrationfrontend.views.html.confirm_delete_section

class ConfirmDeleteSectionViewSpec extends ViewSpecHelper {
  val confirm_delete_section_view: confirm_delete_section = views.confirm_delete_section
  object Selectors {
    val body = "govuk-body"
    val heading = "govuk-heading-l"
    val button = "govuk-button"
  }

  val backLink = "#"
  val section = "1"
  val timestamp = 11111

  "confirm_delete_section_view for Storage Premises" should {
    val pageName = "other_storage_premises"
    val html = confirm_delete_section_view(pageName, section, timestamp)(using request, Messages)
    val document = doc(html)
    "have the expected title" in {
      document
        .title() mustEqual "Are you sure you want to delete Storage premises 1? - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK"
    }
    "should have the expected heading" in {
      document
        .getElementsByClass(Selectors.heading)
        .text() mustEqual "Are you sure you want to delete Storage premises 1?"
    }
    "contain the correct button" in {
      document
        .getElementsByClass(Selectors.button)
        .text() mustBe "Delete Storage premises 1"
    }
    "contain the correct content" in {
      document
        .getElementsByClass(Selectors.body)
        .text() `contains` "Storage premises 1 will be removed from your application."
    }
    testBackLink(document, backLink)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }

  "confirm_delete_section_view for CompanyOfficers" should {
    val pageName = "company_officers"
    val html = confirm_delete_section_view(pageName, section, timestamp)(using request, Messages)
    val document = doc(html)
    "have the expected title" in {
      document
        .title() mustEqual "Are you sure you want to delete Company officer 1? - Apply for the Fulfilment House Due Diligence Scheme - GOV.UK"
    }
    "should have the expected heading" in {
      document
        .getElementsByClass(Selectors.heading)
        .text() mustEqual "Are you sure you want to delete Company officer 1?"
    }
    "contain the correct button" in {
      document
        .getElementsByClass(Selectors.button)
        .text() mustBe "Delete Company officer 1"
    }
    "contain the correct content" in {
      document
        .getElementsByClass(Selectors.body)
        .text() mustBe "Company officer 1 will be removed from your application. Delete Company officer 1"
    }
    testBackLink(document, backLink)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
