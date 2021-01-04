/*
 * Copyright 2021 HM Revenue & Customs
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
  * @param name      will be the name of that value
  * @param context   is optional and will separate many components of
  *                  the same name in the same form e.g. repeating sections
  * @param label     the label for the actual question
  * @param hintText  any hint text for the question
  * @param hasErrors boolean to inform styling of component and use of error msg
  */
case class TextInputParams(
  name: String,
  context: Option[String] = None,
  hiddenNested: Boolean = false,
  value: Option[String] = None,
  label: String,
  hintText: Option[String] = None,
  error: String,
  hasErrors: Boolean = false,
  isNumber: Boolean = false,
  isEmail: Boolean = false,
  hiddenLabel: Boolean = false,
  labelAsHeading: Boolean = false
)
