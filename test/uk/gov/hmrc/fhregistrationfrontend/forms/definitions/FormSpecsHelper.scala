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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import org.scalatest.matchers.should.Matchers
import play.api.data.Form

trait FormSpecsHelper[T] extends Matchers {

  def form: Form[T]

  def dataFromValidForm(post: Map[String, String]): T = {
    val bound = form.bind(post)

    bound.errors shouldBe empty
    bound.value.isDefined shouldBe true
    bound.value.get
  }

  def formDataHasErrors(post: Map[String, String], expected: Seq[(String, String)]): Unit = {
    val result = form bind post
    result.value.isDefined shouldBe false
    val errors = result.errors.flatMap(v => v.messages.map(m => v.key -> m))
    errors should not be empty
    errors should contain allElementsOf expected
  }

  def fieldHasErrors(key: String, value: String, errors: String*) =
    formDataHasErrors(Map(key -> value), errors map { key -> _ } toList)

  def formRequires(fields: String*) =
    formDataHasErrors(
      Map.empty,
      fields map { _ -> "error.required" }
    )
}
