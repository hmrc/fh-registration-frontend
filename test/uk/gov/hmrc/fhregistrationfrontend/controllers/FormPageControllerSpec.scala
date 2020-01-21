/*
 * Copyright 2020 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.actions.JourneyRequestBuilder
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.{BusinessPartnersForm, ContactPersonForm, TradingNameForm}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerType
import uk.gov.hmrc.fhregistrationfrontend.services.{AddressAuditService, Save4LaterKeys}
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, CacheMapBuilder, FormTestData, Save4LaterMocks}

import scala.concurrent.Future

class FormPageControllerSpec
    extends ControllerSpecWithGuiceApp with ActionsMock with Save4LaterMocks with BeforeAndAfterEach {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockSave4Later,
      mockActions,
      addressAuditService
    )
    when(addressAuditService.auditAddresses(any(), any())(any())).thenReturn(Future successful true)
  }

  val addressAuditService = mock[AddressAuditService]
  val controller = new FormPageController(commonDependencies, addressAuditService, mockMcc, mockActions)(
    mockSave4Later,
    scala.concurrent.ExecutionContext.Implicits.global)

  "load" should {
    "Render the page" in {
      val page = Page.mainBusinessAddressPage
      setupPageAction(page)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.load(page.id))(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.main_business_address.title"))
    }
  }

  "loadWithSection" should {
    "Render the page" in {
      val page = Page.companyOfficersPage
      setupPageAction(page)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.loadWithSection(page.id, "1"))(request))

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.company_officers.title"))
    }
  }

  "save" should {
    "Render the form error page" in {
      val page = Page.mainBusinessAddressPage
      setupPageAction(page)

      val request = FakeRequest()
      val result = await(csrfAddToken(controller.save(page.id))(request))

      status(result) shouldBe BAD_REQUEST
      bodyOf(result) should include(Messages("fh.generic.errorPrefix"))
      bodyOf(result) should include(Messages("fh.main_business_address.title"))
    }

    "Redirect to next page" in {
      val page = Page.tradingNamePage
      setupPageAction(page)
      setupSave4Later()

      val request = FakeRequest().withFormUrlEncodedBody(
        TradingNameForm.hasTradingNameKey → "true",
        TradingNameForm.tradingNameKey → "Dodgy Co"
      )
      val result = await(csrfAddToken(controller.save(page.id))(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/form/vatNumber")
    }

    "send an audit address event" in {
      val page = Page.contactPersonPage
      setupPageAction(page)
      setupSave4Later()

      val form = Seq(
        ContactPersonForm.lastNameKey → "last",
        ContactPersonForm.firstNameKey → "first",
        ContactPersonForm.telephoneKey → "0771231231",
        ContactPersonForm.jobTitleKey → "job",
        ContactPersonForm.usingSameContactAddressKey → "false",
        ContactPersonForm.isUkAddressKey → "true"
      )
      val addressForm = Seq(
        "Line1" → "Some",
        "postcode" → "AA11 1AA"
      ).map { case (k, v) ⇒ s"${ContactPersonForm.otherUkContactAddressKey}.$k" -> v }

      val request = FakeRequest().withFormUrlEncodedBody((form ++ addressForm): _*)
      val result = await(csrfAddToken(controller.save(page.id))(request))
      status(result) shouldBe SEE_OTHER
      verify(addressAuditService).auditAddresses(any(), any())(any())
    }

    "Redirect to summary" in {
      val page = Page.tradingNamePage
      setupPageAction(page, journeyPages = JourneyRequestBuilder.fullyCompleteJourney())
      setupSave4Later()

      val request = FakeRequest().withFormUrlEncodedBody(
        TradingNameForm.hasTradingNameKey → "true",
        TradingNameForm.tradingNameKey → "Dodgy Co"
      )
      val result = await(csrfAddToken(controller.save(page.id))(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }

    "Redirect to savedForLater" in {
      val page = Page.tradingNamePage
      setupPageAction(page)
      setupSave4Later()

      val request = FakeRequest().withFormUrlEncodedBody(
        TradingNameForm.hasTradingNameKey → "true",
        TradingNameForm.tradingNameKey → "Dodgy Co",
        "saveAction" → "saveForLater"
      )
      val result = await(csrfAddToken(controller.save(page.id))(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/saved")
    }
  }

  "saveWithSection" should {

    "Redirect to next page" in {
      val page = Page.businessPartnersPage
      setupPageAction(page)
      setupSave4Later()

      val request = FakeRequest().withFormUrlEncodedBody(
        businessPartnerFormData(addMore = false).toSeq: _*
      )

      val result = await(csrfAddToken(controller.saveWithSection(page.id, "1"))(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/form/businessStatus")
    }

    "Redirect to next section" in {
      val page = Page.businessPartnersPage
      setupPageAction(page)
      setupSave4Later()

      val request = FakeRequest().withFormUrlEncodedBody(
        businessPartnerFormData(addMore = true).toSeq: _*
      )

      val result = await(csrfAddToken(controller.saveWithSection(page.id, "1"))(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/form/businessPartners/2")
    }

  }

  "confirmDeleteSection" should {
    "Return an error if the page expired" in {
      val userLastSavedTime = System.currentTimeMillis()
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, userLastSavedTime)
        .cacheMap

      val page = Page.businessPartnersPage

      setupPageAction(page)
      setupSave4LaterFrom(cacheMap)

      val request = FakeRequest()

      val result = await(csrfAddToken(controller.confirmDeleteSection(page.id, "1", userLastSavedTime - 1000))(request))

      status(result) shouldBe NOT_FOUND
    }

    "Render the confirmation page" in {
      val userLastSavedTime = System.currentTimeMillis()
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, userLastSavedTime)
        .cacheMap

      val page = Page.businessPartnersPage

      setupPageAction(page, journeyPages = JourneyRequestBuilder.fullyCompleteJourney(), cacheMap = cacheMap)

      setupSave4LaterFrom(cacheMap)

      val request = FakeRequest()

      val result = await(csrfAddToken(controller.confirmDeleteSection(page.id, "1", userLastSavedTime))(request))

      val expectedSectionName = Messages(s"fh.${page.id}.each.title", "1")

      status(result) shouldBe OK
      bodyOf(result) should include(Messages("fh.delete_confirmation_section.delete", expectedSectionName))
    }
  }

  "deleteSection" should {
    "Return an error if last update timestamp do not match" in {
      val userLastSavedTime = System.currentTimeMillis()
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, userLastSavedTime)
        .cacheMap

      val page = Page.businessPartnersPage

      setupPageAction(page)
      setupSave4LaterFrom(cacheMap)

      val request = FakeRequest()

      val result = await(csrfAddToken(controller.deleteSection(page.id, "1", userLastSavedTime - 1000))(request))

      status(result) shouldBe NOT_FOUND

    }

    "Return an error if section can not be deleted" in {
      val userLastSavedTime = System.currentTimeMillis()
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, userLastSavedTime)
        .cacheMap

      val page = Page.otherStoragePremisesPage

      setupPageAction(page, cacheMap = cacheMap)
      setupSave4LaterFrom(cacheMap)

      val request = FakeRequest()

      val result = await(csrfAddToken(controller.deleteSection(page.id, "any", userLastSavedTime))(request))

      status(result) shouldBe BAD_REQUEST

    }

    "Redirect to the next page" in {
      val userLastSavedTime = System.currentTimeMillis()
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, userLastSavedTime)
        .cacheMap

      val page = Page.businessPartnersPage withData FormTestData.partners

      setupPageAction(page, cacheMap = cacheMap, journeyPages = JourneyRequestBuilder.partialJourneyWithSection)
      setupSave4LaterFrom(cacheMap)

      val request = FakeRequest()

      val result = await(csrfAddToken(controller.deleteSection(page.id, "1", userLastSavedTime))(request))

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/form/businessStatus")
    }
  }

  def businessPartnerFormData(addMore: Boolean) = {
    val addressForm = Map(
      s"${BusinessPartnersForm.addressKey}.Line1" → "Some Line 1",
      s"${BusinessPartnersForm.addressKey}.postcode" → "AA1 1AA"
    )

    val individualPartner = Map(
      BusinessPartnersForm.firstNameKey → "George",
      BusinessPartnersForm.lastNameKey → "Costanza",
      BusinessPartnersForm.hasNationalInsuranceNumberKey → "false"
    ) ++ addressForm

    individualPartner.map {
      case (k, v) ⇒ s"${BusinessPartnersForm.businessPartnerIndividualKey}.$k" -> v
    } +
      (BusinessPartnersForm.businessPartnersTypeKey → BusinessPartnerType.Individual.toString) +
      ("addMore" → addMore.toString)
  }
}
