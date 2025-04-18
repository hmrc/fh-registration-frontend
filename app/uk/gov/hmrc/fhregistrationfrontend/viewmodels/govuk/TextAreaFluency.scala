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

package viewmodels.govuk

import play.api.data.Field
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.FormGroup
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.label.Label
import uk.gov.hmrc.govukfrontend.views.viewmodels.textarea.Textarea
import viewmodels.ErrorMessageAwareness

object textArea extends TextAreaFluency

trait TextAreaFluency {

  object TextAreaViewModel extends ErrorMessageAwareness {

    def apply(
      field: Field,
      label: Label
    )(implicit messages: Messages): Textarea =
      Textarea(
        id = field.id,
        name = field.name,
        value = field.value,
        label = label,
        errorMessage = errorMessage(field)
      )
  }

  implicit class FluentTextArea(textArea: Textarea) {

    def withId(id: String): Textarea =
      textArea copy (id = id)

    def describedBy(value: String): Textarea =
      textArea copy (describedBy = Some(value))

    def withHint(hint: Hint): Textarea =
      textArea copy (hint = Some(hint))

    def withFormGroupClasses(classes: FormGroup): Textarea =
      textArea copy (formGroup = classes)

    def withCssClass(newClass: String): Textarea =
      textArea copy (classes = s"${textArea.classes} $newClass")

    def withAutocomplete(value: String): Textarea =
      textArea copy (autocomplete = Some(value))

    def withAttribute(attribute: (String, String)): Textarea =
      textArea copy (attributes = textArea.attributes + attribute)

    def withSpellcheck(on: Boolean = true): Textarea =
      textArea copy (spellcheck = Some(on))
  }
}
