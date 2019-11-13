/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import akka.stream.Materializer
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import play.filters.csrf.CSRFAddToken

trait ControllerSpecWithGuiceApp extends ControllersSpecBase with GuiceOneAppPerSuite with I18nSupport {

  val messagesApi = app.injector.instanceOf(classOf[MessagesApi])
  val Messages = request2Messages(FakeRequest())
  implicit val materializer = mock[Materializer]

  val commonDependencies = app.injector.instanceOf(classOf[CommonPlayDependencies])
  val csrfAddToken: CSRFAddToken = app.injector.instanceOf[play.filters.csrf.CSRFAddToken]

  implicit val mockMcc: MessagesControllerComponents = app.injector.instanceOf[MessagesControllerComponents]
}
