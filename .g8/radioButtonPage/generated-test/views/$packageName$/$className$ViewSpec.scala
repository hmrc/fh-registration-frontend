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
    val errorSummaryList = "govuk-list govuk-error-summary__list"
    val button = "govuk-button"
    val form = "form"
  }

  val $className;format="Camel"$ = $className$("1", "2")
  val $className;format="Camel"$JsObject = Json.toJson($className;format="Camel"$).as[JsObject].value
  val $className;format="Camel"$Map: collection.Map[String, String] =
  $className;format="Camel"$JsObject.map { case (fName, fValue) => fName -> fValue.toString }

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

      "include the $option1Value$ radio" in {
        val questionItem = radioItems
          .get(0)
        questionItem
          .getElementsByClass(Selectors.label)
          .text() mustBe "$option1Value$"
      }

      "include the $option2Value$ field" in {
        val questionItem = radioItems
          .get(1)
        questionItem
          .getElementsByClass(Selectors.label)
          .text() mustBe "$option2Value$"
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

    "value is empty" should {
      val fieldWithError = Map("value" -> "")
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), call, backLink)(request, Messages, appConfig)
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
          .attr("href") mustBe "#$option1Name$"
        errorSummary.text() mustBe Messages("fh.$packageName$.$className;format="decap"$.error.$field1Name$.required")
      }
    }

    testBackLink(document, backLink)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
