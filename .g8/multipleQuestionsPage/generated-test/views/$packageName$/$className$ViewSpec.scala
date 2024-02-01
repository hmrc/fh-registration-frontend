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
import models.{CheckMode, NormalMode}
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import play.api.i18n.Messages
import play.api.mvc.{Call, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.fhregistrationfrontend.views.html.$packageName$.v2.$className$View
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.fhregistrationfrontend.views.ViewSpecHelper
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig



class $className$ViewSpec extends ViewSpecHelper {

  val view = app.injector.instanceOf[$className$View]
  val form = $className$Form.form
  implicit val request: Request[_] = FakeRequest()
  lazy val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

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
    "the form is valid" should {
      val html = view(form, NormalMode)(request, messages(application))
      val document = doc(html)
      val questionItems = document.getElementsByClass(Selectors.formGroup)
      "contain the expected title" in {
        document.title() must include("$title$")
      }

      "have the expected heading" in {
        document.getElementsByClass(Selectors.heading).text() mustEqual "$heading$"
      }

      "contain 2 questions" in {
        questionItems.size() mustBe 2
      }

      "include the $field1Name$ field" in {
        val questionItem = questionItems
          .get(0)
        questionItem
          .getElementsByClass(Selectors.label)
          .text() mustBe "$field1Name$"
        )
      }

      "include the $field2Name$ field" in {
        val questionItem = questionItems
          .get(1)
        questionItem
          .getElementsByClass(Selectors.label)
          .text() mustBe "$field2Name$"
        )
      }

      "contain the correct button" in {
        document.getElementsByClass(Selectors.button).text() mustBe "Save and continue"
      }

      "contains a form with the correct action" in {
        val htmlAllSelected = view(form.fill($className;format="Camel"$), call, backLink)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual call.url
      }
    }

    "$field1Name$ is empty" should {
      val fieldWithError = Map("$field1Name$" -> "", "$field2Name$" -> "test")
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), call, backLink)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "have a title containing error" in {
        val titleMessage = Messages("fh.$packageName$.$className;format="decap"$.title")
        documentWithErrors.title must include("Error: $title$")
      }

      "have a title containing error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#$field1Name$"
        errorSummary.text() mustBe Messages("fh.$packageName$.$className;format="decap"$.error.$field1Name$.required")
      }
    }

    "$field2Name$ is empty" should {
      val fieldWithError = Map("$field1Name$" -> "test", "$field2Name$" -> "")
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), call, backLink)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "have a title containing error" in {
        val titleMessage = Messages("fh.$packageName$.$className;format="
        decap"$.title"
        )
        documentWithErrors.title must include("Error: $title$")
      }

      "have a title containing error" in {
        val errorSummary = documentWithErrors
          .getElementsByClass(Selectors.errorSummaryList)
          .first()
        errorSummary
          .select("a")
          .attr("href") mustBe "#$field2Name$"
        errorSummary.text() mustBe Messages("fh.$packageName$.$className;format="decap"$.error.$field2Name$.required")
      }
    }

    testBackLink(document, backLink)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
