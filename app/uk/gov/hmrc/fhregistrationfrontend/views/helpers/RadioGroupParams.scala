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

import play.twirl.api.Html

case class RadioGroupParams(
  name: Option[String] = None,
  radios: List[RadioParams],
  inline: Boolean = true,
  legend: Option[String] = None,
  legendAsHeading: Boolean = false,
  legendSize: Option[String] = None,
  error: Option[String] = None,
  value: Option[String] = None,
  hintText: Option[String] = None,
  hintHtml: Option[Html] = None,
  hasErrors: Boolean = false
)