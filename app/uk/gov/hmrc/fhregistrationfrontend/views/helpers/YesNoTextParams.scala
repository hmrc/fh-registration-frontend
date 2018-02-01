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
  */
case class YesNoTextParams (
                            name: String,
                            context: Option[String] = None,
                            label: String,
                            hintText: Option[String] = None,
                            yesInputLabel: String,
                            yesInputHintText: Option[String] = None,
                            targetForNo: Option[String] = None,
                            noInputLabel: Option[String] = None,
                            noInputHintText: Option[String] = None,
                            formHasErrors: Boolean = false
                            )

