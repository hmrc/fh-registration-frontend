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

package uk.gov.hmrc.fhregistrationfrontend.forms.journeys

import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.ImportingActivitiesForm
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class ImportingActivitiesPageSpec extends UnitSpec with MockitoSugar {

  private val mainPage = BasicPage(
    "hasEori",
    ImportingActivitiesForm.hasEoriForm,
    mock[FormRendering[Boolean]]
  )
  private val eoriNumberPage = BasicPage(
    "enterEORI",
    ImportingActivitiesForm.eoriNumberOnlyForm,
    mock[FormRendering[String]]
  )
  private val goodsPage = BasicPage(
    "importingGoodsNotBelongingToBusiness",
    ImportingActivitiesForm.goodsImportedOutsideEoriOnlyForm,
    mock[FormRendering[Boolean]]
  )

  private val enterEoriSubsection = Some("enterEORI")
  private val goodsSubsection = Some("importingGoodsNotBelongingToBusiness")

  private val answeredFalseToHasEori = ImportingActivitiesPage(mainPage withData false, eoriNumberPage, goodsPage)
  private val answeredTrueToHasEori = ImportingActivitiesPage(mainPage withData true, eoriNumberPage, goodsPage)
  private val answeredTrueToHasEoriAndEoriNumberInputted = ImportingActivitiesPage(
    mainPage withData true,
    eoriNumberPage withData "1234123132",
    goodsPage
  ).withSubsection(enterEoriSubsection)
  private val answeredTrueToHasEoriAndEoriNumberAndGoodsInputted = ImportingActivitiesPage(
    mainPage withData true,
    eoriNumberPage withData "1234123132",
    goodsPage withData true
  ).withSubsection(goodsSubsection)

  "Next section" should {
    "Be None" when {
      "Answer is No on main section" in {
        answeredFalseToHasEori.nextSubsection shouldBe None
      }

      "on goods subsection" in {
        answeredTrueToHasEoriAndEoriNumberAndGoodsInputted.nextSubsection shouldBe None
      }
    }

    "Be enter eori subsection" when {
      "Answer is Yes on main section" in {
        answeredTrueToHasEori.nextSubsection.get shouldEqual enterEoriSubsection.get
      }
    }

    "Be goods subsection" when {
      "on enter eori subsection" in {
        answeredTrueToHasEoriAndEoriNumberInputted.nextSubsection.get shouldEqual goodsSubsection.get
      }
    }

  }

  "Previous section" should {
    "Be none" when {
      "Answer is No on main section" in {
        answeredFalseToHasEori.previousSubsection shouldBe None
      }

      "Answer is Yes on main section" in {
        answeredTrueToHasEori.previousSubsection shouldBe None
      }
    }

    "Be mainsection" when {
      "Answer is Yes on main section" in {
        answeredTrueToHasEoriAndEoriNumberInputted.previousSubsection.get shouldEqual "any"
      }
    }

    "Be enter eori subsection" when {
      "on goods subsection" in {
        answeredTrueToHasEoriAndEoriNumberAndGoodsInputted.previousSubsection.get shouldEqual enterEoriSubsection.get
      }
    }

  }

}
