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

import uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.routes
import uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$.$className$Form
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.views.html.$packageName$.v2.$className$View
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.fhregistrationfrontend.views.ViewSpecHelper
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig


class $className$ViewSpec extends ViewSpecHelper {

  val view: $className$View = app.injector.instanceOf[$className$View]
  val form: Form[$className$.Value] = $className$Form.form

  val backLink = "http://test.com"
  val call: Call = Call("GET", "/foo")

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

      "have the expected heading" in {
        //Todo update with capture
        val expectedHeader = "this is for: $packageName$ testRadios"
        document.getElementsByClass(Selectors.heading).text() mustEqual expectedHeader
      }

      "have the expected radio buttons" that {
        "are all unchecked" when {
          val radioButtons = document.getElementsByClass(Selectors.radios)
          "the form is empty" in {
            val radioButton1 = radioButtons
              .get(0)
            val radioButton2 = radioButtons
              .get(1)
            radioButton1
              .getElementsByClass(Selectors.radioLables)
              .text() mustBe "$option1msg$"
            radioButton2
              .getElementsByClass(Selectors.radioLables)
              .text() mustBe "$option2msg$"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .attr("value") mustBe "$option1key$"
            radioButton2
              .getElementsByClass(Selectors.radioInput)
              .attr("value") mustBe "$option2key$"
            radioButton1
              .getElementsByClass(Selectors.radioInput)
              .hasAttr("checked") mustBe false
            radioButton2
              .getElementsByClass(Selectors.radioInput)
              .hasAttr("checked") mustBe false
          }
        }

        $className$.values.foreach { selectedRadioItem =>
          "has " + selectedRadioItem.toString + "checked" when {
            "the form is prepopulated with " + selectedRadioItem.toString in {
              val htmlSelected = view(form.fill(selectedRadioItem), call, backLink)(request, Messages, appConfig)
              val documentSelected = doc(htmlSelected)
              val radioButtons = documentSelected.getElementsByClass(Selectors.radios)
              radioButton1
                .getElementsByClass(Selectors.radioLables)
                .text() mustBe "$option1msg$"
              radioButton2
                .getElementsByClass(Selectors.radioLables)
                .text() mustBe "$option2msg$"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .attr("value") mustBe "$option1key$"
              radioButton2
                .getElementsByClass(Selectors.radioInput)
                .attr("value") mustBe "$option2key$"
              radioButton1
                .getElementsByClass(Selectors.radioInput)
                .hasAttr("checked") mustBe selectedRadioItem.toString == "$option1key$"
              radioButton2
                .getElementsByClass(Selectors.radioInput)
                .hasAttr("checked") mustBe selectedRadioItem.toString == "$option2key$"
            }
          }
        }
      }

      "contain the correct button" in {
        document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
      }

      "contains a form with the correct action" in {
        val htmlAllSelected = view(form.fill(TestRadios.option1), call, backLink)(request, Messages, appConfig)
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected
          .select(Selectors.form)
          .attr("action") mustEqual call.url
      }
    }

    "form errors exist" should {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), call, backLink)(request, Messages, appConfig)
      val documentWithErrors = doc(htmlWithErrors)

      "have a title containing error" in {
        documentWithErrors.title must include("Error: $title$")
      }

      "have a error summary" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#value"
        errorSummary.text() mustBe Messages("fh.$packageName$.$className;format="decap"$.error.required")
      }
    }

    testBackLink(document, backLink)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
