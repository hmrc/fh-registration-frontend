/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.actions

import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{ContactPerson, MainBusinessAddress}
import uk.gov.hmrc.fhregistrationfrontend.teststubs.StubbedErrorHandler

class PageActionSpec extends ActionSpecBase with JourneyRequestBuilder {

  "Page Action" should {
    "Fail when the page is not found " in {
      val request = journeyRequest()

      val action = new PageAction("some-page-id", None)(StubbedErrorHandler)
      status(result(action, request)) shouldBe NOT_FOUND
    }

    "Load the required page" in {
      val request = journeyRequest()

      val action = new PageAction(Page.contactPersonPage.id, None)(StubbedErrorHandler)

      val refined = refinedRequest(action, request)

      refined.userId shouldBe request.userId
      refined.journeyState shouldBe request.journeyState
      refined.lastUpdateTimestamp shouldBe request.lastUpdateTimestamp

      refined.page.id shouldBe Page.contactPersonPage.id
      refined.page.data shouldBe None
    }

    "Fail if page is inaccessible" in {
      val request = journeyRequest()

      val action = new PageAction(Page.mainBusinessAddressPage.id, None)(StubbedErrorHandler)

      status(result(action, request)) shouldBe NOT_FOUND
    }

    "Fail if a section is requested but does not exist on the page" in {
      val request = journeyRequest()

      val action = new PageAction(Page.contactPersonPage.id, Some("1"))(StubbedErrorHandler)

      status(result(action, request)) shouldBe NOT_FOUND
    }

    "Load page data" in {
      val seqPages = Journeys.partnershipPages map { page ⇒
        page.id match {
          case Page.contactPersonPage.id       ⇒ page.asInstanceOf[Page[ContactPerson]] withData FormTestData.contactPerson
          case Page.mainBusinessAddressPage.id ⇒ page.asInstanceOf[Page[MainBusinessAddress]] withData FormTestData.mainBusinessAddress
          case _                               ⇒ page
        }
      }

      val request = journeyRequest(new JourneyPages(seqPages))
      val action = new PageAction(Page.mainBusinessAddressPage.id, None)(StubbedErrorHandler)

      val refined = refinedRequest(action, request)
      refined.page.data shouldBe Some(FormTestData.mainBusinessAddress)

    }

    "Load page section" in {
      val onePage = Page.companyOfficersPage withData FormTestData.companyOfficers

      val request = journeyRequest(new JourneyPages(Seq(onePage)))

      val action = new PageAction(Page.companyOfficersPage.id, Some("2"))(StubbedErrorHandler)
      val refined = refinedRequest(action, request)
      refined.page.section shouldBe Some("2")
      refined.page.data shouldBe Some(FormTestData.companyOfficers)
    }
  }


}
