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

import org.mockito.Mockito.reset
import org.scalatest.{BeforeAndAfter, BeforeAndAfterEach}
import play.api.mvc.MessagesControllerComponents
import play.api.test.FakeRequest
import uk.gov.hmrc.fhregistrationfrontend.actions.JourneyRequestBuilder
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType.JourneyType
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{JourneyType, Journeys}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessType
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, Save4LaterMocks}
import uk.gov.hmrc.fhregistrationfrontend.views.Mode

class SummaryControllerSpec extends ControllerSpecWithGuiceApp
  with ActionsMock
{

  val mockControllerComponents = mock[MessagesControllerComponents]
  val controller = new SummaryController(commonDependencies, mockControllerComponents, mockActions)

  "summary" should {
    "Render the summary html for all journey types" in {
      for {
        journeyType ← JourneyType.values
      } {
        setupSummaryAction(
          journeyPages = JourneyRequestBuilder.fullyCompleteJourney(),
          journeyType = journeyType
        )

        val request = FakeRequest()
        val result = await(csrfAddToken(controller.summary())(request))

        status(result) shouldBe OK
        bodyOf(result) should include(Messages(s"fh.summary.${expectedMode(journeyType)}.title"))

        reset(mockActions)
      }
    }

    "Render the summary html for all business types" in {
      for {
        (businessType, pages) ← List(
          BusinessType.Partnership → Journeys.partnershipPages,
          BusinessType.SoleTrader → Journeys.soleTraderPages,
          BusinessType.CorporateBody → Journeys.limitedCompanyPages)
      } {
        setupSummaryAction(
          journeyPages = JourneyRequestBuilder.fullyCompleteJourney(pages),
          businessType = businessType
        )

        val request = FakeRequest()
        val result = await(csrfAddToken(controller.summary())(request))

        status(result) shouldBe OK
        bodyOf(result) should include(Messages(s"fh.summary.${Mode.Amendment}.title"))

        reset(mockActions)
      }
    }
  }

  def expectedMode(journeyType: JourneyType) = journeyType match {
    case JourneyType.Amendment ⇒ Mode.Amendment
    case JourneyType.New       ⇒ Mode.New
    case JourneyType.Variation ⇒ Mode.Variation
  }


}
