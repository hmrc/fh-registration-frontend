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

import java.io.InputStream

import play.api.libs.json.Json
import uk.gov.hmrc.fhddsfrontend.models.CompaniesHouse.companySearchResultReader
import uk.gov.hmrc.play.test.UnitSpec

class CompaniesHouseSpec extends UnitSpec {

  "Parse Json" should {
    "read a companies house result" in {
      val stream : InputStream = getClass.getResourceAsStream("/models/companiesHouseSearchResult.json")
      val result = Json.parse(stream ).as[CompanySearchResult]
      result.items.size shouldBe 3
      result.items.map(_.companyNumber) shouldBe List("01234567", "02123456", "03123456")
      result.items.map(_.title) shouldBe List(
        "COMPANY A",
        "COMPANY B",
        "COMPANY C"
      )
      result.items.map(_.address.postalCode) shouldBe List("GL00 0AA", "WC00 0AA", "CM00 0AA")
      result.items.map(_.address.addressLine1) shouldBe List(Some("Some Road"), None, Some("Some Street"))


    }
  }


}
