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

package uk.gov.hmrc.fhregistrationfrontend.models

import org.scalatest.EitherValues
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{EoriNumber, ImportingActivities}
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class ImportingActivitiesSpec extends UnitSpec with EitherValues {

  "Importing Activities Model" should {
    val importingActivitiesWithValues: ImportingActivities =
      ImportingActivities(hasEori = true, eori = Some("1234123132"), goodsImported = Some(false))
    val importingActivitiesWithEoriNumberModel: ImportingActivities =
      ImportingActivities(hasEori = true, eoriNumber = Some(EoriNumber("6789678687", true)))
    val importingActivitiesWithNeither: ImportingActivities = ImportingActivities(hasEori = false)

    "derive eori number from eori value if eori value there" in {
      importingActivitiesWithValues.eoriValue shouldBe Some("1234123132")
    }

    "fallback to derive eori number from eori number model if eori value not there and eori number model there" in {
      importingActivitiesWithEoriNumberModel.eoriValue shouldBe Some("6789678687")
    }

    "return None for eori number if neither eori value or eori number model is there" in {
      importingActivitiesWithNeither.eoriValue shouldBe None
    }

    "derive goods imported from goods imported value if goods imported value there" in {
      importingActivitiesWithValues.goodsImportedValue shouldBe Some(false)
    }

    "fallback to derive goods imported from eori number model if goods imported value not there and eori number model there" in {
      importingActivitiesWithEoriNumberModel.goodsImportedValue shouldBe Some(true)
    }

    "return None for goods imported if neither goods imported value or eori number model is there" in {
      importingActivitiesWithNeither.goodsImportedValue shouldBe None
    }
  }

}
