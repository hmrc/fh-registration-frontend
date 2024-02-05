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

package uk.gov.hmrc.fhregistrationfrontend.views.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$.$className$Form
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import play.api.mvc.Call
import uk.gov.hmrc.fhregistrationfrontend.views.html.$packageName$.v2.$className$View
import uk.gov.hmrc.fhregistrationfrontend.views.ViewSpecHelper



class $className$ViewSpec extends ViewSpecHelper {

  val view = app.injector.instanceOf[$className$View]
  val form = $className$Form.form

  val backLink = "http://test.com"
  val call = Call("GET", "/foo")

  object Selectors {
    val formGroup = "govuk-form-group"
    val labelAsHeading = "govuk-label  govuk-label--m"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  val $className;format="Camel"$ = "testing123"
  "View" when {
    val html = view(form, call, backLink)(request, Messages, appConfig)
    val document = doc(html)
    val formGroup = document.getElementsByClass(Selectors.formGroup)
    "the form is valid" should {
      "contain the expected title" in {
        document.title() must include("$title$")
      }

      "have the expected heading" in {
        //Todo update expected header so caption rather than packag
        val expectedHeader = "this is for: $packageName$ $heading$"
        document.getElementsByClass(Selectors.labelAsHeading).text() mustEqual expectedHeader
      }

      "should contain a text input field" in {
        formGroup.size() mustBe 1
        formGroup.get(0).getElementsByClass(Selectors.labelAsHeading)
          .text() mustBe Messages("$packageName$.$className;format="decap"$.heading"
        )
      }

      "contain the correct button" in {
        document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
      }

      "contains a form with the correct action" in {
        val htmlAllSelected = view(form.fill($className;format="Camel"$), call, backLink)(request, Messages, appConfig)
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual call.url
      }
    }

    "form errors exist" should {
      val fieldWithError = Map("value" -> "")
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), call, backLink)(request, Messages, appConfig)
      val documentWithErrors = doc(htmlWithErrors)

      "have a title containing error" in {
        val titleMessage = Messages("fh.$packageName$.$className;format="decap"$.title")
        documentWithErrors.title must include("Error: $title$")
      }

      "contains a message that links to field with error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe Messages("Enter $className;format="decap"$"
        )
      }
    }

    testBackLink(document, backLink)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
