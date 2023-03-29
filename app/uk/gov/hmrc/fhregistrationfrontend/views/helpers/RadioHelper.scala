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

package uk.gov.hmrc.fhregistrationfrontend.views.helpers

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.NationalInsuranceNumberForm._
import uk.gov.hmrc.fhregistrationfrontend.utils.ViewUtils
import uk.gov.hmrc.fhregistrationfrontend.views.html.components.text_input
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

import javax.inject.Inject

class RadioHelper @Inject()(input: text_input) extends ViewUtils {

  def conditionalYesNoRadio(
    form: Form[_],
    hasKey: String,
    legendContent: String,
    inputKey: String,
    hintText: String
  )(implicit msgs: Messages): Seq[RadioItem] =
    Seq(
      RadioItem(
        value = Some("true"),
        content = Text(msgs("fh.generic.yes")),
        checked = form(hasKey).value.contains("true"),
        conditionalHtml = Some(
          html(
            input(
              form = form,
              legendContent = msgs(legendContent),
              inputKey = inputKey,
              hintText = Some(msgs(hintText)),
              id = form(hintText).name
            )
          )
        )
      ),
      RadioItem(
        id = Some(form("nationalInsuranceNumber_yesNo").name),
        value = Some("false"),
        content = Text(msgs("fh.generic.no")),
        checked = form(hasNationalInsuranceNumberKey).value.contains("false")
      )
    )

}
