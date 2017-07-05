/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.fhddsfrontend.controllers

import akka.stream.Materializer
import org.scalatestplus.play.OneAppPerSuite
import play.api.http.Status
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.HeaderNames
import uk.gov.hmrc.play.test.UnitSpec

class ApplicationControllerSpec extends UnitSpec with OneAppPerSuite {

  val appInjector: Injector = app.injector
  implicit val materializer: Materializer = appInjector.instanceOf[Materializer]
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(HeaderNames.xSessionId -> "test")
  implicit val messages = Messages(Lang.defaultLang, appInjector.instanceOf[MessagesApi])

  val applicationController = new Application(messages.messages)

  "GET /" should {
    "return 200" in {
      val result = applicationController.start().apply(request)
      result.header.status shouldBe Status.OK
    }

    "return HTML" in {
      val result = applicationController.start().apply(request)
      charset(result) shouldBe Some("utf-8")
    }

  }

}
