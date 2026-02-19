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

package uk.gov.hmrc.fhregistrationfrontend.repositories

import play.api.libs.json.Json
import uk.gov.hmrc.fhregistrationfrontend.controllers.ControllerSpecWithGuiceApp

class SessionRepositorySpec extends ControllerSpecWithGuiceApp {

  private val repository = app.injector.instanceOf[SessionRepository]

  "SessionRepository.setEntries" should {
    "upsert entries and read them back" in {
      val id = "session-repo-test"
      val entries = Map(
        "foo" -> Json.toJson("bar"),
        "num" -> Json.toJson(1)
      )

      repository.setEntries(id, entries).futureValue shouldBe true

      val fetched = repository.get(id).futureValue
      fetched.isDefined shouldBe true
      fetched.get.getEntry[String]("foo") shouldBe Some("bar")
      fetched.get.getEntry[Int]("num") shouldBe Some(1)

      repository.clear(id).futureValue shouldBe true
    }
  }
}
