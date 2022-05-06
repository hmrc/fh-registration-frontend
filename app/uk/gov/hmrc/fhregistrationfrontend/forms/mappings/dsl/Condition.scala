/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl

import play.api.data.Mapping

trait Condition {

  def eval(data: Map[String, String]): Boolean
  def withPrefix(prefix: String): Condition
}

class ConditionIs[T](mapping: Mapping[T], value: T) extends Condition {
  override def eval(data: Map[String, String]): Boolean =
    mapping
      .bind(data)
      .fold(
        _ ⇒ false,
        _ == value
      )

  override def withPrefix(prefix: String): Condition = new ConditionIs(mapping withPrefix prefix, value)
}
