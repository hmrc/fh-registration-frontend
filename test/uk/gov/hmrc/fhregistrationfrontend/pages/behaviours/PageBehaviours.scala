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

package uk.gov.hmrc.fhregistrationfrontend.pages.behaviours

import models.UserAnswers
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues, TryValues}
import pages.QuestionPage
import play.api.libs.json._
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

trait PageBehaviours extends UnitSpec with ScalaFutures with BeforeAndAfterEach with Matchers with OptionValues with TryValues {

  class BeRetrievable[A] {
    def apply(page: QuestionPage[A], value: A)(implicit formatter: Format[A]): Unit = {

      "the user answers does not contain page data" should {
        "return None" in {
          val userAnswers = UserAnswers("testId")

          userAnswers.get[A](page) shouldBe empty
        }
      }

      "the user answers contains page data" should {
        "return the value" in {
          val userAnswers = UserAnswers("testId")
            .set[A](page, value).success.value

          userAnswers.get[A](page) shouldBe Some(value)
        }
      }
    }
  }

  class BeSettable[A] {
    def apply(page: QuestionPage[A], value: A)(implicit formatter: Format[A]): Unit = {
      "must be able to be set on UserAnswers" in {
        val userAnswers = UserAnswers("testId")
          .set[A](page, value).success.value

        userAnswers.get[A](page) shouldBe Some(value)
      }
    }
  }

  class BeRemovable[A] {
    def apply(page: QuestionPage[A], value: A)(implicit formatter: Format[A]): Unit = {

      "must be able to be removed from UserAnswers" in {
        val userAnswers = UserAnswers("testId")
          .set[A](page, value).success.value

        userAnswers.get[A](page) shouldBe Some(value)

        val updatedAnswers = userAnswers.remove(page).success.value
        updatedAnswers.get(page) shouldBe empty
      }
    }
  }

  def beRetrievable[A]: BeRetrievable[A] = new BeRetrievable[A]

  def beSettable[A]: BeSettable[A] = new BeSettable[A]

  def beRemovable[A]: BeRemovable[A] = new BeRemovable[A]
}
