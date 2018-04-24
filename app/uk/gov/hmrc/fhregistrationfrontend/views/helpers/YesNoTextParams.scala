/*
 * Copyright 2018 HM Revenue & Customs
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

/**
  * Created by ali on 01/02/18.
  *
  * for the YesNoText component, where a target value is optional
  * we can ask if the value is to be supplied using Yes/No radio buttons
  * these params are required to use the component:
  *
  * @param name            will be the name of that target value
  * @param context         is optional and will separate many components of
  *                        the same name in the same form e.g. repeating sections
  * @param yesNoLabel      the label for the actual Yes/No question
  * @param yesNoHintText   any hint text for the Yes/No question
  * @param valueLabel      the label for the target value if Yes has been answered
  * @param valueHintText   any hint text for the revealed target value field
  * @param hasValueError   boolean to say whether target field has errors
  * @param targetForNo     an optional field name to be required if No is answered
  * @param noInputLabel    label for field required if No is answered
  * @param noInputHintText hint text for the field required if No is answered
  * @param hasNoValueError boolean to say whether the target field for No has errors
  * @param hasErrors       boolean to say whether the yes/no radio has errors (e.g. is empty)
  */

case class YesNoTextParams(
  name           : String,
  context        : Option[String] = None,
  value          : Option[String] = None,
  yesNoValue     : Option[String] = None,
  yesNoLabel     : String,
  yesNoHintText  : Option[String] = None,
  yesNoError     : String,
  valueLabel     : String,
  valueHintText  : Option[String] = None,
  valueError     : String,
  hasValueError  : Boolean = false,
  targetForNo    : Option[String] = None,
  noInputLabel   : Option[String] = None,
  noInputHintText: Option[String] = None,
  noInputError   : Option[String] = None,
  hasNoValueError: Boolean = false,
  hasErrors      : Boolean = false,
  legendAsHeading   : Boolean = false
)

