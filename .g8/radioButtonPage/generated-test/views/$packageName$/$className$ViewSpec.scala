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
import uk.gov.hmrc.fhregistrationfrontend.views.html.$packageName$.v2.$className$View
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.fhregistrationfrontend.views.ViewSpecHelper
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig


class $className$ViewSpec extends ViewSpecHelper {

  val view = app.injector.instanceOf[$className$View]
  val form = $className$Form.form
  //  implicit val request: Request[_] = FakeRequest()
  //  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val backLink = "http://test.com"
  val call = Call("GET", "/foo")

  object Selectors {
    val heading = "govuk-heading-l"
    val formGroup = "govuk-form-group"
    val label = "govuk-label"
    val radios = "govuk-radios"
    val radiosInput = "govuk-radios__input"
    val radiosItems = "govuk-radios__item"
    val radiosLabels = "govuk-label govuk-radios__label"
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  "View" when {
    val html = view(form, call, backLink)(request, Messages, appConfig)
    val document = doc(html)
    val radioItems = document.getElementsByClass(Selectors.formGroup)
    "the form is valid" should {
      "contain the expected title" in {
        document.title() must include("$title$")
      }

      "have the expected heading" in {
        //Todo update expected header so caption rather than package
        val expectedHeader = "this is for: $packageName$ $heading$"
        document.getElementsByClass(Selectors.heading).text() mustEqual expectedHeader
      }

      "contain 2 radio options" in {
        radioItems.size() mustBe 2
      }

      "include the $className$.Value.head radio" in {
        val radioItem = radioItems
          .get(0)
        radioItem
          .getElementsByClass(Selectors.label)
          .text() mustBe "$className$.Value.head.toString"
      }

      "include the $className$.Value.last field" in {
        val radioItem = radioItems
          .get(1)
        radioItem
          .getElementsByClass(Selectors.label)
          .text() mustBe "$className$.Value.last.toString"
      }

      "contain the correct button" in {
        document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
      }

      "contains a form with the correct action" in {
        val htmlAllSelected = view(form.fill($className$.Value.head), call, backLink)(request, Messages, appConfig)
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual call.url
      }
    }

    $className$.values.foreach { radio =>
      val html1 = view(form.fill(radio), call, backLink)(request, Messages, appConfig)
      val document1 = doc(html1)

      s"when the form is preoccupied with " + radio.toString + "selected and has no errors" in {
        "should have radioButtons" in {
          val radioButtons = document1.getElementsByClass(Selectors.radiosItems)
          $className$.values.zipWithIndex.foreach { case (radio1, index) =>
            if (radio1.toString == radio.toString) {
              s"that has the option to select" + radio1.toString + " and is checked" in {
                val radioButtons1 = radioButtons
                  .get(index)
                radioButtons1
                  .getElementsByClass(Selectors.radiosLabels)
                  .text() mustBe Messages("fh.companyOfficers.$packageName$.$className;format="decap"$." + radio1.toString)
                val input = radioButtons1
                  .getElementsByClass(Selectors.radiosInput)
                input.attr("value") mustBe radio1.toString
                input.hasAttr("checked") mustBe true
              }
            } else {
              s"that has the option to select " + radio1.toString + " and is unchecked" in {
                val radiobuttons1 = radioButtons
                  .get(index)
                radiobuttons1
                  .getElementsByClass(Selectors.radiosLabels)
                  .text() mustBe Messages("fh.$packageName$.$className;format="decap"$." + radio1.toString)
                val input = radiobuttons1
                  .getElementsByClass(Selectors.radiosInput)
                input.attr("value") mustBe radio1.toString
                input.hasAttr("checked") mustBe false
              }
            }
          }
        }
      }
    }

    "value is empty" should {
      val htmlWithErrors = view(form.bind(Map("value" -> "")), call, backLink)(request, Messages, appConfig)
      val documentWithErrors = doc(htmlWithErrors)

      "have a title containing error" in {
        val titleMessage = Messages("fh.$packageName$.$className;format="decap"$.title")
        documentWithErrors.title must include("Error: $title$")
      }

      "have a error summary" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#"
        errorSummary.text() mustBe Messages("fh.$packageName$.$className;format="decap"$.error.required")
      }
    }

    testBackLink(document, backLink)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
