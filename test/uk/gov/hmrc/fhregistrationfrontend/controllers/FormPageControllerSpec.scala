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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import com.codahale.metrics.SharedMetricRegistries
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.actions.JourneyRequestBuilder
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.{BusinessPartnersForm, CompanyOfficersForm, ContactPersonForm, TradingNameForm, VatNumberForm}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.CompanyOfficerType
import uk.gov.hmrc.fhregistrationfrontend.models.businessPartners.BusinessPartnerType
import uk.gov.hmrc.fhregistrationfrontend.services.{AddressAuditService, Save4LaterKeys}
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{ActionsMock, CacheMapBuilder, FormTestData, Save4LaterMocks}

import scala.concurrent.Future

class FormPageControllerSpec
    extends ControllerSpecWithGuiceApp with ActionsMock with Save4LaterMocks with BeforeAndAfterEach {

  SharedMetricRegistries.clear()

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockSave4Later,
      mockActions,
      addressAuditService
    )
    when(addressAuditService.auditAddresses(any(), any())(any())).thenReturn(Future successful true)
  }

  val addressAuditService: AddressAuditService = mock[AddressAuditService]
  val controller = new FormPageController(commonDependencies, addressAuditService, mockMcc, mockActions, views)(
    mockSave4Later,
    scala.concurrent.ExecutionContext.Implicits.global
  )

  "load" should {
    "Render the page" in {
      setupPageAction(mainBusinessAddressPage)

      val request = FakeRequest()
      val result = csrfAddToken(controller.load(mainBusinessAddressPage.id))(request)

      status(result) shouldBe OK
      contentAsString(result) should include(Messages("fh.main_business_address.title"))
    }
  }

  "loadWithSection" should {
    "Render the page" in {
      setupPageAction(companyOfficersPage)

      val request = FakeRequest()
      val result = csrfAddToken(controller.loadWithSection(companyOfficersPage.id, "1"))(request)

      status(result) shouldBe OK
      contentAsString(result) should include(Messages("fh.company_officers.title"))
    }
  }

  "save" should {
    "Render the form error page" in {
      setupPageAction(mainBusinessAddressPage)

      val request = FakeRequest()
      val result = csrfAddToken(controller.save(mainBusinessAddressPage.id))(request)

      status(result) shouldBe BAD_REQUEST
      contentAsString(result) should include(Messages("fh.generic.errorPrefix"))
      contentAsString(result) should include(Messages("fh.main_business_address.title"))
    }

    "Redirect to next page" in {
      setupPageAction(tradingNamePage)
      setupSave4Later()

      val request = FakeRequest().withFormUrlEncodedBody(
        TradingNameForm.hasTradingNameKey -> "true",
        TradingNameForm.tradingNameKey    -> "Dodgy Co"
      )
      val result = csrfAddToken(controller.save(tradingNamePage.id))(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/form/vatNumber")
    }

    "send an audit address event" in {
      setupPageAction(contactPersonPage)
      setupSave4Later()

      val form = Seq(
        ContactPersonForm.lastNameKey                -> "last",
        ContactPersonForm.firstNameKey               -> "first",
        ContactPersonForm.telephoneKey               -> "0771231231",
        ContactPersonForm.jobTitleKey                -> "job",
        ContactPersonForm.usingSameContactAddressKey -> "false",
        ContactPersonForm.isUkAddressKey             -> "true"
      )
      val addressForm = Seq(
        "Line1"    -> "Some",
        "postcode" -> "AA11 1AA"
      ).map { case (k, v) => s"${ContactPersonForm.otherUkContactAddressKey}.$k" -> v }

      val request = FakeRequest().withFormUrlEncodedBody((form ++ addressForm): _*)
      val result = csrfAddToken(controller.save(contactPersonPage.id))(request)
      status(result) shouldBe SEE_OTHER
      verify(addressAuditService).auditAddresses(any(), any())(any())
    }

    "Render vat number error form if vat number used elsewhere in journey" in {
      setupPageAction(vatNumberPage, journeyPages = JourneyRequestBuilder.fullyCompleteJourney())
      setupSave4Later()

      val request = FakeRequest().withFormUrlEncodedBody(
        VatNumberForm.hasVatNumberKey -> "true",
        VatNumberForm.vatNumberKey    -> "223456789"
      )
      val result = csrfAddToken(controller.save(vatNumberPage.id))(request)

      status(result) shouldBe BAD_REQUEST
    }

    "Render business partner error form if vat number used elsewhere in journey" in {
      setupPageAction(businessPartnersPage, journeyPages = JourneyRequestBuilder.fullyCompleteJourney())
      setupSave4Later()

      val request = FakeRequest().withFormUrlEncodedBody(
        businessPartnerFormData(addMore = true, vatNumber = "123456789").toSeq: _*
      )
      val result = csrfAddToken(controller.saveWithSection(businessPartnersPage.id, "1"))(request)

      status(result) shouldBe BAD_REQUEST
    }

    "Render company officer error form if vat number used elsewhere in journey" in {
      setupPageAction(companyOfficersPage, journeyPages = JourneyRequestBuilder.fullyCompleteJourney())
      setupSave4Later()

      val companyOfficerCompany = Map(
        CompanyOfficersForm.companyNameKey     -> "George Co",
        CompanyOfficersForm.hasVatKey          -> "true",
        CompanyOfficersForm.vatRegistrationKey -> "123456789",
        CompanyOfficersForm.roleKey            -> "Director"
      )

      val companyOfficersFormData = companyOfficerCompany.map { case (k, v) =>
        s"companyIdentification.$k" -> v
      } +
        (CompanyOfficersForm.identificationTypeKey -> CompanyOfficerType.Company.toString) +
        ("addMore"                                 -> true.toString)

      val request = FakeRequest().withFormUrlEncodedBody(
        companyOfficersFormData.toSeq: _*
      )
      val result = csrfAddToken(controller.saveWithSection(companyOfficersPage.id, "1"))(request)

      status(result) shouldBe BAD_REQUEST
    }

    "Redirect to summary" in {
      setupPageAction(tradingNamePage, journeyPages = JourneyRequestBuilder.fullyCompleteJourney())
      setupSave4Later()

      val request = FakeRequest().withFormUrlEncodedBody(
        TradingNameForm.hasTradingNameKey -> "true",
        TradingNameForm.tradingNameKey    -> "Dodgy Co"
      )
      val result = csrfAddToken(controller.save(tradingNamePage.id))(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/summary")
    }

    "Redirect to savedForLater" in {
      setupPageAction(tradingNamePage)
      setupSave4Later()

      val request = FakeRequest()
        .withFormUrlEncodedBody(
          TradingNameForm.hasTradingNameKey -> "true",
          TradingNameForm.tradingNameKey    -> "Dodgy Co",
          "saveAction"                      -> "saveForLater"
        )
        .withMethod("POST")
      val result = csrfAddToken(controller.save(tradingNamePage.id))(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/saved")
    }
  }

  "saveWithSection" should {

    "Redirect to next page" in {
      setupPageAction(businessPartnersPage)
      setupSave4Later()

      val request = FakeRequest().withFormUrlEncodedBody(
        businessPartnerFormData(addMore = false).toSeq: _*
      )

      val result = csrfAddToken(controller.saveWithSection(businessPartnersPage.id, "1"))(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/form/businessStatus")
    }

    "Redirect to next section" in {
      setupPageAction(businessPartnersPage)
      setupSave4Later()

      val request = FakeRequest().withFormUrlEncodedBody(
        businessPartnerFormData(addMore = true).toSeq: _*
      )

      val result = csrfAddToken(controller.saveWithSection(businessPartnersPage.id, "1"))(request)

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

      setupPageAction(businessPartnersPage)
      setupSave4LaterFrom(cacheMap)

      val request = FakeRequest()

      val result = csrfAddToken(
        controller.confirmDeleteSection(businessPartnersPage.id, "1", userLastSavedTime - 1000)
      )(request)

      status(result) shouldBe NOT_FOUND
    }

    "Render the confirmation page" in {
      val userLastSavedTime = System.currentTimeMillis()
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, userLastSavedTime)
        .cacheMap

      setupPageAction(
        businessPartnersPage,
        journeyPages = JourneyRequestBuilder.fullyCompleteJourney(),
        cacheMap = cacheMap
      )

      setupSave4LaterFrom(cacheMap)

      val request = FakeRequest()

      val result = csrfAddToken(
        controller.confirmDeleteSection(businessPartnersPage.id, "1", userLastSavedTime)
      )(request)

      val expectedSectionName = Messages(s"fh.${businessPartnersPage.id}.each.title", "1")

      status(result) shouldBe OK
      contentAsString(result) should include(Messages("fh.delete_confirmation_section.delete", expectedSectionName))
    }
  }

  "deleteSection" should {
    "Return an error if last update timestamp do not match" in {
      val userLastSavedTime = System.currentTimeMillis()
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, userLastSavedTime)
        .cacheMap

      setupPageAction(businessPartnersPage)
      setupSave4LaterFrom(cacheMap)

      val request = FakeRequest()

      val result = csrfAddToken(
        controller.deleteSection(businessPartnersPage.id, "1", userLastSavedTime - 1000)
      )(request)

      status(result) shouldBe NOT_FOUND

    }

    "Return an error if section can not be deleted" in {
      val userLastSavedTime = System.currentTimeMillis()
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, userLastSavedTime)
        .cacheMap

      val otherStoragePremises = page.otherStoragePremisesPage
      setupPageAction(otherStoragePremises, cacheMap = cacheMap)
      setupSave4LaterFrom(cacheMap)

      val request = FakeRequest()

      val result = csrfAddToken(
        controller.deleteSection(otherStoragePremises.id, "any", userLastSavedTime)
      )(request)

      status(result) shouldBe BAD_REQUEST

    }

    "Redirect to the next page" in {
      val userLastSavedTime = System.currentTimeMillis()
      val cacheMap = CacheMapBuilder(testUserId)
        .withValue(Save4LaterKeys.userLastTimeSavedKey, userLastSavedTime)
        .cacheMap

      val page = businessPartnersPage withData FormTestData.partners

      setupPageAction(page, cacheMap = cacheMap, journeyPages = JourneyRequestBuilder.partialJourneyWithSection)
      setupSave4LaterFrom(cacheMap)

      val request = FakeRequest()

      val result = csrfAddToken(
        controller.deleteSection(businessPartnersPage.id, "1", userLastSavedTime)
      )(request)

      status(result) shouldBe SEE_OTHER
      redirectLocation(result) shouldBe Some("/fhdds/form/businessStatus")
    }
  }

  def businessPartnerFormData(addMore: Boolean, vatNumber: String = "111222333") = {
    val addressForm = Map(
      s"${BusinessPartnersForm.addressKey}.Line1"    -> "Some Line 1",
      s"${BusinessPartnersForm.addressKey}.postcode" -> "AA1 1AA"
    )

    val soleProprietorPartner = Map(
      BusinessPartnersForm.firstNameKey                  -> "George",
      BusinessPartnersForm.lastNameKey                   -> "Costanza",
      BusinessPartnersForm.hasTradeNameKey               -> "false",
      BusinessPartnersForm.hasNationalInsuranceNumberKey -> "false",
      BusinessPartnersForm.hasVatKey                     -> "true",
      BusinessPartnersForm.vatRegistrationKey            -> vatNumber
    ) ++ addressForm

    soleProprietorPartner.map { case (k, v) =>
      s"${BusinessPartnersForm.businessPartnerSoleProprietorKey}.$k" -> v
    } +
      (BusinessPartnersForm.businessPartnersTypeKey -> BusinessPartnerType.SoleProprietor.toString) +
      ("addMore"                                    -> addMore.toString)
  }
}
