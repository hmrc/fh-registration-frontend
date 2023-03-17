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

  def conditionalYesNoRadio(form: Form[_])(implicit msgs: Messages): Seq[RadioItem] = {
    println(Console.MAGENTA_B + s"Form: $form" + Console.RESET)
    println(Console.MAGENTA_B + s"Form ID: ${form(nationalInsuranceNumberKey).id}" + Console.RESET)
    println(Console.MAGENTA_B + s"Form ID: ${form(nationalInsuranceNumberKey).errors}" + Console.RESET)
    println(Console.MAGENTA_B + s"Form errors: ${form.error(nationalInsuranceNumberKey)}" + Console.RESET)

    Seq(
      RadioItem(
        value = Some("true"),
        content = Text(msgs("fh.generic.yes")),
        checked = form(hasNationalInsuranceNumberKey).value.contains("true"),
        conditionalHtml = Some(
          html(
            input(
              form = form,
              legendContent = msgs("fh.national_insurance_number.title"),
              inputKey = nationalInsuranceNumberKey,
              hintText = Some(msgs("fh.partner.national_insurance_number.hintText")),
              id = form(nationalInsuranceNumberKey).name
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

}
