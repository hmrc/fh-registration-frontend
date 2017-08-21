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

package uk.gov.hmrc.fhddsfrontend.connectors

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.test.FakeRequest
import uk.gov.hmrc.fhddsfrontend.AppUnitGenerator
import uk.gov.hmrc.fhddsfrontend.models._
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


class FhddsConnectorSpec extends AppUnitGenerator {

  val CHConnector = mock[FhddsConnector]

  "Companies House Connector" should {
    "Use HTTP Get to connect to Companies House" in {
      val expected = CompanyDetails(
        Some(Address("")),
        Some(Company("COMPANY", Some("12334"), Some("ltd"), Some("active"), None)))
      when(
        CHConnector.http.GET[CompanyDetails](any(), any())(any(), any())
      ).thenReturn(Future successful expected)

      implicit val hc = HeaderCarrier.fromHeadersAndSession(FakeRequest().headers)
      expected shouldBe Await.result(CHConnector.lookupCompanyDetails()(hc), Duration.apply("10 seconds"))
    }
  }

}
