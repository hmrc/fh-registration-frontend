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

package uk.gov.hmrc.fhddsfrontend.models

import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec

class CompaniesHouseConfigSpec extends UnitSpec with OneAppPerSuite {

  "Auth Encoder" should {
    "get codes" in {
      val result = CompaniesHouseConfig.getCompaniesHouseAuthCode
      val expectResult = "aVZuf_mhBlMZSTSBtJSX7w_MzAJ3pFHe6ajMZeaO:"
      result shouldBe expectResult
    }
    "encode codes to base64 format" in {
      val code : String = "aVZuf_mhBlMZSTSBtJSX7w_MzAJ3pFHe6ajMZeaO"
      val result = CompaniesHouseConfig.encodeAuthCode(code)
      val expectResult = "YVZadWZfbWhCbE1aU1RTQnRKU1g3d19NekFKM3BGSGU2YWpNWmVhTw=="
      result shouldBe expectResult
    }
  }

}
