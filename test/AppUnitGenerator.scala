/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend

import akka.stream.Materializer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.inject.Injector
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.filters.csrf.CSRFAddToken
import uk.gov.hmrc.auth.core.PlayAuthConnector
import uk.gov.hmrc.fhregistrationfrontend.connectors.{BusinessCustomerFrontendConnector, FhddsConnector}
import uk.gov.hmrc.fhregistrationfrontend.controllers.CommonPlayDependencies
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

import scala.concurrent.ExecutionContextExecutor

trait AppUnitGenerator
    extends UnitSpec with ScalaFutures with GuiceOneAppPerSuite with MockitoSugar with BeforeAndAfterEach {

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(1, Seconds), interval = Span(50, Millis))
  implicit val executionContext: ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.Implicits.global.asInstanceOf[ExecutionContextExecutor]

  val appInjector: Injector = app.injector

  implicit val materializer: Materializer = appInjector.instanceOf[Materializer]
  implicit val csrfAddToken: CSRFAddToken = app.injector.instanceOf[play.filters.csrf.CSRFAddToken]
  implicit val request: FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest().withHeaders(HeaderNames.xSessionId -> "test")
  implicit val headerCarrier: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers)

  val mockFhddsConnector: FhddsConnector = mock[FhddsConnector]
  val mockBusinessCustomerFrontendConnector: BusinessCustomerFrontendConnector = mock[BusinessCustomerFrontendConnector]

  val mockAuthConnector = mock[PlayAuthConnector]
  val mockConfiguration = mock[Configuration]
  val ds: CommonPlayDependencies = app.injector.instanceOf[CommonPlayDependencies]
  val mockSave4Later = mock[Save4LaterService]
}
