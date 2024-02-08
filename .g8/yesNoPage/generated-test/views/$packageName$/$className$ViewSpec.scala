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
    val heading = "govuk-fieldset__heading"
    val legend = "govuk-fieldset__legend  govuk-fieldset__legend--l"
    val radios = "govuk-radios__item"
    val radioInput = "govuk-radios__input"
    val radioLables = "govuk-label govuk-radios__label"
    val body = "govuk-body"
    val errorSummaryTitle = "govuk-error-summary__title"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  "View" when {
    val html = view(form, call, backLink)(request, Messages, appConfig)
    val document = doc(html)
    "the form is valid" should {
      "contain the expected title" in {
        document.title() must include("$title$")
      }

      "should include a legend with the expected heading" in {
        //Todo update expected header so caption rather than packag
        val expectedHeader = "this is for: $packageName$ $heading$"
        document.getElementsByClass(Selectors.legend).text() mustEqual expectedHeader
      }

      "the form is not preoccupied and has no errors" should {

        "have radio buttons" that {
          val radioButtons = document.getElementsByClass(Selectors.radios)
          "has the option to select Yes and is unchecked" in {
            val radioButton1 = radioButtons
              .get(0)
            radioButton1
              .getElementsByClass(Selectors.radioLables)
              .text() mustBe "Yes"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .attr("value") mustBe "true"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .hasAttr("checked") mustBe false
          }

          "has the option to select No and is unchecked" in {
            val radioButton1 = radioButtons
              .get(1)
            radioButton1
              .getElementsByClass(Selectors.radioLables)
              .text() mustBe "No"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .attr("value") mustBe "false"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .hasAttr("checked") mustBe false
          }
        }
      }

      "the form is preoccupied with yes and has no errors" should {
        val html1 = view(form.fill(true), call, backLink)(request, Messages, appConfig)
        val document1 = doc(html1)
        "should have radio buttons" that {
          val radioButtons = document1.getElementsByClass(Selectors.radios)
          "has the option to select Yes and is checked" in {
            val radioButton1 = radioButtons
              .get(0)
            radioButton1
              .getElementsByClass(Selectors.radioLables)
              .text() mustBe "Yes"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .attr("value") mustBe "true"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .hasAttr("checked") mustBe true
          }

          "has the option to select No and is unchecked" in {
            val radioButton1 = radioButtons
              .get(1)
            radioButton1
              .getElementsByClass(Selectors.radioLables)
              .text() mustBe "No"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .attr("value") mustBe "false"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .hasAttr("checked") mustBe false
          }
        }
      }

      "the form is preoccupied with no and has no errors" should {
        val html1 = view(form.fill(false), call, backLink)(request, Messages, appConfig)
        val document1 = doc(html1)
        "should have radio buttons" that {
          val radioButtons = document1.getElementsByClass(Selectors.radios)
          "has the option to select Yes and is unchecked" in {
            val radioButton1 = radioButtons
              .get(0)
            radioButton1
              .getElementsByClass(Selectors.radioLables)
              .text() mustBe "Yes"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .attr("value") mustBe "true"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .hasAttr("checked") mustBe false
          }

          "has the option to select No and is checked" in {
            val radioButton1 = radioButtons
              .get(1)
            radioButton1
              .getElementsByClass(Selectors.radioLables)
              .text() mustBe "No"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .attr("value") mustBe "false"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .hasAttr("checked") mustBe true
          }
        }
      }

      "contain the correct button" in {
        document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
      }

      "contains a form with the correct action" when {
        "yes is selected" in {
          val htmlAllYesSelected = view(form.fill(true), call, backLink) (request, Messages, appConfig)
          val documentAllSelected = doc(htmlAllYesSelected)

          documentAllSelected.select(Selectors.form)
            .attr("action") mustEqual call.url
        }

        "no is selected" in {
          val htmlAllYesSelected = view(form.fill(false), call, backLink) (request, Messages, appConfig)
          val documentAllSelected = doc(htmlAllYesSelected)

          documentAllSelected.select(Selectors.form)
            .attr("action") mustEqual call.url
        }
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
        errorSummary.text() mustBe Messages("Select yes if $className;format="decap"$")
      }
    }

    testBackLink(document, backLink)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
