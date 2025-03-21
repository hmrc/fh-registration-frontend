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

package uk.gov.hmrc.fhregistrationfrontend.actions

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.BeforeAndAfterEach
import play.api.test.Helpers._
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.teststubs.UserTestData
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

import scala.concurrent.{Await, Future, Promise}

trait ActionSpecBase
    extends UnitSpec with ScalaFutures with MockitoSugar with BeforeAndAfterEach with Matchers with Results with Status
    with UserTestData {

  def refinedRequest[P[_], R[_], A](action: ActionRefiner[R, P], request: R[A]) = {
    val p = Promise[P[A]]()
    val result = action.invokeBlock(
      request,
      { (r: P[A]) =>
        p.success(r)
        Future(Ok)
      }
    )

    status(result) shouldBe OK
    await(p.future)
  }

  def result[P[_], R[_], A](action: ActionFunction[R, P], request: R[A]) =
    action.invokeBlock(
      request,
      (r: P[A]) => Future(Ok)
    )

}
