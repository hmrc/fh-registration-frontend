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

package uk.gov.hmrc.fhddsfrontend

import akka.stream.Materializer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.OneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.inject.Injector
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.filters.csrf.CSRFAddToken
import uk.gov.hmrc.fhddsfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhddsfrontend.controllers.CommonPlayDependencies
import uk.gov.hmrc.play.http.HeaderNames
import uk.gov.hmrc.play.test.UnitSpec


trait AppUnitGenerator extends UnitSpec with ScalaFutures with OneAppPerSuite with MockitoSugar with BeforeAndAfterEach {

  implicit val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  override implicit val patienceConfig = PatienceConfig(timeout = Span(1, Seconds), interval = Span(50, Millis))

  val appInjector: Injector = app.injector
  implicit val materializer: Materializer = appInjector.instanceOf[Materializer]
  implicit val csrfAddToken: CSRFAddToken = app.injector.instanceOf[play.filters.csrf.CSRFAddToken]
  implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders(HeaderNames.xSessionId -> "test")
  implicit val messages = Messages(Lang.defaultLang, appInjector.instanceOf[MessagesApi])
  val bc: FhddsConnector = mock[FhddsConnector]
  val ds: CommonPlayDependencies = app.injector.instanceOf[CommonPlayDependencies]

}
