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

package uk.gov.hmrc.fhregistrationfrontend.models.formmodel

import play.api.data.{Field, Form}

case class InputTypeTextParams(field: Field,
                               parentForm: Option[Form[_]] = None,
                               inputId: Option[String] = None,
                               inputClass: Option[String] = None,
                               divClass: Option[String] = None,
                               labelClass: Option[String] = None,
                               label: Option[String] = None,
                               labelDataAttributes: Option[String] = None,
                               wrapperClass: Option[String] = None,
                               labelHighlight: Boolean = false,
                               required: Boolean = false,
                               formHint: Option[String] = None,
                               formHintId: Option[String] = None,
                               maxLength: Option[String] = None,
                               dataAttributes: Option[String] = None,
                               isPostCode: Boolean = false,
                               postCodeLookupAddressNumber: Option[String] = None,
                               additionalHiddenLabelText: Option[String] = None,
                               ariaLabelledBy: Option[String] = None,
                               formHasErrors: Boolean = false,
                               fieldName: Option[String] = None,
                               isDate: Boolean = false
                              )
