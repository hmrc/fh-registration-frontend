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

package uk.gov.hmrc.fhregistrationfrontend.forms.mappings

import org.scalatest.matchers.should.Matchers
import play.api.data.Mapping

trait MappingSpecsHelper[T] extends Matchers {
  def mapping: Mapping[T]

  def dataFromValidForm(post: Map[String, String]) = {
    val bound = mapping bind post

    bound.isRight shouldBe true
    bound.right.toOption.isDefined shouldBe true
    bound.right.toOption.get
  }

  def formDataHasErrors(post: Map[String, String], expected: List[(String, String)]): Unit = {
    val result = mapping bind post
    result.isLeft shouldBe true
    val errors = result.left.toOption.get.flatMap(v => v.messages.map(m => v.key → m))
    errors should not be empty
    errors should contain allElementsOf expected
  }

}
