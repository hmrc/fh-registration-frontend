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
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{JourneyPages, Page}
import uk.gov.hmrc.fhregistrationfrontend.teststubs.StubbedErrorHandler

class SummaryActionSpec extends ActionSpecBase with JourneyRequestBuilder {

  val action = new SummaryAction()(StubbedErrorHandler)

  "Summary Action" should {
    "Fail if journey is not complete" in {
      val request = journeyRequest()

      status(result(action, request)) shouldBe BAD_REQUEST
    }

    "Work when the journey is complete" in {
      val onePage = Page.contactPersonPage withData FormTestData.contactPerson
      val request = journeyRequest(new JourneyPages(Seq(onePage)))

      val refined = refinedRequest(action, request)

      refined.userId shouldBe request.userId
      refined.registrationNumber shouldBe request.registrationNumber
      refined.bpr shouldBe request.bpr
      refined.businessType shouldBe request.businessType
      refined.verifiedEmail shouldBe request.verifiedEmail

      refined.pageDataOpt(Page.contactPersonPage) shouldBe Some(FormTestData.contactPerson)

    }


  }

}
