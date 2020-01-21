/*
 * Copyright 2020 HM Revenue & Customs
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

object MappingsApi {
  implicit class MappingOps[T](val m: Mapping[T]) {

    def is(value: T) = new ConditionIs[T](m, value)

    def onlyWhen(condition: Condition) = OnlyWhen(m, condition)
  }

  implicit class MappingWithKeyOps[T](val v: (String, Mapping[T])) {

    def is(value: T) = new ConditionIs[T](v._2 withPrefix v._1, value)
  }

}
