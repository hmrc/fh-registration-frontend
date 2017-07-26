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
import uk.gov.hmrc.fhddsfrontend.AppUnitGenerator
import uk.gov.hmrc.fhddsfrontend.models.{Company, CompanyAddress, CompanySearchResult}
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}


class CompaniesHouseConnectorSpec extends AppUnitGenerator {
  object CHConnector extends CompaniesHouseConnector  {
    override val http: WSHttp = mock[WSHttp]
    override val url: String = "https://api.companieshouse.gov.uk"
    override val authHeader: String = "Basic YVZadWZfbWhCbE1aU1RTQnRKU1g3d19NekFKM3BGSGU2YWpNWmVhTzo="
  }


  "Companies House Connector" should {
    "Use HTTP Get to connect to Companies House" in {
      val expected = List(Company(
        "COMPANY", "12334", "ltd", "active", CompanyAddress("AB1 2CD")))
      when(
        CHConnector.http.GET[CompanySearchResult](any(), any())(any(), any())
      ).thenReturn(Future successful CompanySearchResult(expected))

      expected shouldBe Await.result(CHConnector.fuzzySearchCompany("COMPANY"), Duration.apply("10 seconds"))
    }
  }




}
