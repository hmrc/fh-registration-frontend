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

import controllers.$packageName$.routes
import forms.$packageName$.$className$FormProvider
import models.{CheckMode, NormalMode}
import models.$packageName$.$className$
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import views.html.$packageName$.$className$View
import play.api.libs.json.{JsObject, Json}
import views.ViewSpecHelper



class $className$ViewSpec extends ViewSpecHelper {

  val view = application.injector.instanceOf[$className$View]
  val formProvider = new $className$FormProvider
  val form = formProvider.apply()
  implicit val request: Request[_] = FakeRequest()

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

  "View" - {
    val html = view(form, NormalMode)(request, messages(application))
    val document = doc(html)
    val questionItems = document.getElementsByClass(Selectors.formGroup)
    "should contain the expected title" in {
      document.title() must include(Messages("$packageName$.$className;format="decap"$" + ".title"))
    }

    "should have the expected heading" in {
      document.getElementsByClass(Selectors.heading).text() mustEqual Messages("$packageName$.$className;format="decap"$" + ".heading")
    }

    "should contain" + $className;format="Camel"$Map.size + " questions" in {
      questionItems.size() mustBe $className;format="Camel"$Map.size
    }

    $className;format="Camel"$Map.zipWithIndex.foreach { case ((fieldName, fieldValue), index) =>

      "when the form is not prepopulated and has no errors" - {
        "should include the expected question fields" - {

          "that has the field " + fieldName in {
            val questionItem1 = questionItems
              .get(index)
            questionItem1
              .getElementsByClass(Selectors.label)
              .text() mustBe Messages("$packageName$.$className;format="decap"$." + fieldName)
          }
        }
      }
    }

    "contain the correct button" - {
      document.getElementsByClass(Selectors.button).text() mustBe Messages("site.continue")
    }

    "contains a form with the correct action" - {
      "when in CheckMode" in {
        val htmlAllSelected = view(form.fill($className;format="Camel"$), CheckMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.$className$Controller.onSubmit(CheckMode).url
      }

      "when in NormalMode" in {
        val htmlAllSelected = view(form.fill($className;format="Camel"$), NormalMode)(request, messages(application))
        val documentAllSelected = doc(htmlAllSelected)

        documentAllSelected.select(Selectors.form)
          .attr("action") mustEqual routes.$className$Controller.onSubmit(NormalMode).url
      }
    }


    $className;format="Camel"$Map.foreach { case (fieldName, _) =>
      val fieldWithError = $className;format="Camel"$Map + ((fieldName -> ""))
      val htmlWithErrors = view(form.bind(fieldWithError.toMap), NormalMode)(request, messages(application))
      val documentWithErrors = doc(htmlWithErrors)

      "when " + fieldName + "is empty" - {
        "should have a title containing error" in {
          val titleMessage = Messages("$packageName$.$className;format="decap"$.title")
          documentWithErrors.title must include("Error: " + titleMessage)
        }

        "contains a message that links to field with error" in {
          val errorSummary = documentWithErrors
            .getElementsByClass(Selectors.errorSummaryList)
            .first()
          errorSummary
            .select("a")
            .attr("href") mustBe "#" + fieldName
          errorSummary.text() mustBe Messages("$packageName$.$className;format="decap"$.error." + fieldName + ".required")
        }
      }
    }

    testBackLink(document)
    validateTimeoutDialog(document)
    validateTechnicalHelpLinkPresent(document)
    validateAccessibilityStatementLinkPresent(document)
  }
}
