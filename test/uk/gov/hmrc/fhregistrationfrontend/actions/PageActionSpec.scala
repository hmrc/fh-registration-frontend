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

package uk.gov.hmrc.fhregistrationfrontend.actions

import play.api.test.Helpers._
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessPartner, CompanyOfficer, ContactPerson, MainBusinessAddress, VatNumber}
import uk.gov.hmrc.fhregistrationfrontend.teststubs.{FormTestData, StubbedErrorHandler}

class PageActionSpec extends ActionSpecBase with JourneyRequestBuilder {

  "Page Action" should {
    "Fail when the page is not found " in {
      val request = journeyRequest()

      val action =
        new PageAction("some-page-id", None, journeys)(
          StubbedErrorHandler,
          scala.concurrent.ExecutionContext.Implicits.global
        )
      status(result(action, request)) shouldBe NOT_FOUND
    }

    "Load the required page" in {
      val request = journeyRequest()

      val action = new PageAction(contactPersonPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)

      refined.userId shouldBe request.userId
      refined.journeyState shouldBe request.journeyState
      refined.lastUpdateTimestamp shouldBe request.lastUpdateTimestamp

      refined.page.id shouldBe contactPersonPage.id
      refined.page.data shouldBe None
    }

    "Fail if page is inaccessible" in {
      val request = journeyRequest()

      val action = new PageAction(mainBusinessAddressPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      status(result(action, request)) shouldBe NOT_FOUND
    }

    "Fail if a section is requested but does not exist on the page" in {
      val request = journeyRequest()

      val action = new PageAction(contactPersonPage.id, Some("1"), journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      status(result(action, request)) shouldBe NOT_FOUND
    }

    "Load page data" in {
      val seqPages = journeys.partnershipPages map { page =>
        page.id match {
          case contactPersonPage.id => page.asInstanceOf[Page[ContactPerson]] withData FormTestData.contactPerson
          case mainBusinessAddressPage.id =>
            page.asInstanceOf[Page[MainBusinessAddress]] withData FormTestData.mainBusinessAddress
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(mainBusinessAddressPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.page.data shouldBe Some(FormTestData.mainBusinessAddress)

    }

    "Load page section" in {
      val onePage = companyOfficersPage withData FormTestData.companyOfficers

      val request = journeyRequest(journeyPages = new JourneyPages(Seq(onePage)))

      val action = new PageAction(companyOfficersPage.id, Some("2"), journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )
      val refined = refinedRequest(action, request)
      refined.page.section shouldBe Some("2")
      refined.page.data shouldBe Some(FormTestData.companyOfficers)
    }

    "Load other used vat numbers in company officers when passed a vat number - limited company journey" in {
      val seqPages = journeys.limitedCompanyPages map { page =>
        page.id match {
          case companyOfficersPage.id =>
            page.asInstanceOf[RepeatingPage[CompanyOfficer]] withData FormTestData.companyOfficers
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _                => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(vatNumberPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.otherUsedVatNumbersFromVatNumberPage() shouldBe List("523456789", "623456789")
    }

    "Load other used vat numbers in business partners when passed a vat number - business partnership journey" in {
      val seqPages = journeys.partnershipPages map { page =>
        page.id match {
          case businessPartnersPage.id => page.asInstanceOf[RepeatingPage[BusinessPartner]] withData FormTestData.partners
          case vatNumberPage.id        => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _                       => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(vatNumberPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.otherUsedVatNumbersFromVatNumberPage() shouldBe List("223456789", "323456789", "423456789")
    }

    "Load other used vat numbers in business partners when passed a first business partner - business partnership journey" in {
      val seqPages = journeys.partnershipPages map { page =>
        page.id match {
          case businessPartnersPage.id => page.asInstanceOf[RepeatingPage[BusinessPartner]] withData FormTestData.partners
          case vatNumberPage.id        => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _                       => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(businessPartnersPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.otherUsedVatNumbersFromBusinessPartnersPage(
        FormTestData.partners.values.toList,
        index = 0
      ) shouldBe List("223456789", "323456789", "423456789", "123456789")
    }

    "Load other used vat numbers in business partners when passed a second business partner - business partnership journey" in {
      val seqPages = journeys.partnershipPages map { page =>
        page.id match {
          case businessPartnersPage.id => page.asInstanceOf[RepeatingPage[BusinessPartner]] withData FormTestData.partners
          case vatNumberPage.id        => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _                       => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(businessPartnersPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.otherUsedVatNumbersFromBusinessPartnersPage(
        FormTestData.partners.values.toList,
        index = 1
      ) shouldBe List("323456789", "423456789", "123456789")
    }

    "Load other used vat numbers in company officers when passed a first company officer - limited company journey" in {
      val seqPages = journeys.limitedCompanyPages map { page =>
        page.id match {
          case companyOfficersPage.id =>
            page.asInstanceOf[RepeatingPage[CompanyOfficer]] withData FormTestData.companyOfficers
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _                => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(companyOfficersPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.otherUsedVatNumbersFromCompanyOfficersPage(
        FormTestData.companyOfficers.values.toList,
        index = 0
      ) shouldBe List("623456789", "123456789")
    }

    "Load other used vat numbers in company officers when passed a second company officer - limited company journey" in {
      val seqPages = journeys.limitedCompanyPages map { page =>
        page.id match {
          case companyOfficersPage.id =>
            page.asInstanceOf[RepeatingPage[CompanyOfficer]] withData FormTestData.companyOfficers
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _                => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(companyOfficersPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.otherUsedVatNumbersFromCompanyOfficersPage(
        FormTestData.companyOfficers.values.toList,
        index = 1
      ) shouldBe List("523456789", "623456789", "123456789")
    }

    "Check if vat number is unique when passed a unique vat number - limited company journey" in {
      val uniqueVatNumber = FormTestData.vatNumber
      val seqPages = journeys.limitedCompanyPages map { page =>
        page.id match {
          case companyOfficersPage.id =>
            page.asInstanceOf[RepeatingPage[CompanyOfficer]] withData FormTestData.companyOfficers
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData uniqueVatNumber
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(vatNumberPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.isVatNumberUniqueForVatNumberPage(uniqueVatNumber) shouldBe true
    }

    "Check if vat number is unique when passed a non-unique vat number - limited company journey" in {
      val nonUniqueVatNumber = FormTestData.vatNumber.copy(value = Some("523456789"))
      val seqPages = journeys.limitedCompanyPages map { page =>
        page.id match {
          case companyOfficersPage.id =>
            page.asInstanceOf[RepeatingPage[CompanyOfficer]] withData FormTestData.companyOfficers
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData nonUniqueVatNumber
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(vatNumberPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.isVatNumberUniqueForVatNumberPage(nonUniqueVatNumber) shouldBe false
    }

    "Check if vat number is unique when passed a unique vat number - business partnership journey" in {
      val uniqueVatNumber = FormTestData.vatNumber
      val seqPages = journeys.partnershipPages map { page =>
        page.id match {
          case businessPartnersPage.id => page.asInstanceOf[RepeatingPage[BusinessPartner]] withData FormTestData.partners
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData uniqueVatNumber
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(vatNumberPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.isVatNumberUniqueForVatNumberPage(uniqueVatNumber) shouldBe true
    }

    "Check if vat number is unique when passed a non-unique vat number - business partnership journey" in {
      val nonUniqueVatNumber = FormTestData.vatNumber.copy(value = Some("423456789"))
      val seqPages = journeys.partnershipPages map { page =>
        page.id match {
          case businessPartnersPage.id => page.asInstanceOf[RepeatingPage[BusinessPartner]] withData FormTestData.partners
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData nonUniqueVatNumber
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(vatNumberPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.isVatNumberUniqueForVatNumberPage(nonUniqueVatNumber) shouldBe false
    }

    "Check if vat number unique in business partners when passed a first business partner with unique vat number - business partnership journey" in {
      val businessPartnerWithNonUniqueVatNumber = FormTestData.businessPartnerSoleProprietorWithVatNumber(FormTestData.vatNumber.value)
      val businessPartnersWithUniqueFirstVatNumber = FormTestData.partners.updated(1, businessPartnerWithNonUniqueVatNumber)
      val seqPages = journeys.partnershipPages map { page =>
        page.id match {
          case businessPartnersPage.id => page.asInstanceOf[RepeatingPage[BusinessPartner]] withData businessPartnersWithUniqueFirstVatNumber
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(businessPartnersPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.isVatNumberUniqueForBusinessPartner(
        businessPartnersWithUniqueFirstVatNumber.values.toList,
        index = 0
      ) shouldBe true
    }

    "Check if vat number unique in business partners when passed a first business partner with non-unique vat number - business partnership journey" in {
      val businessPartnerWithNonUniqueVatNumber = FormTestData.businessPartnerSoleProprietorWithVatNumber(FormTestData.vatNumber.value)
      val businessPartnersWithNonUniqueFirstVatNumber = FormTestData.partners.updated(0, businessPartnerWithNonUniqueVatNumber)
      val seqPages = journeys.partnershipPages map { page =>
        page.id match {
          case businessPartnersPage.id => page.asInstanceOf[RepeatingPage[BusinessPartner]] withData businessPartnersWithNonUniqueFirstVatNumber
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(businessPartnersPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.isVatNumberUniqueForBusinessPartner(
        businessPartnersWithNonUniqueFirstVatNumber.values.toList,
        index = 0
      ) shouldBe false
    }

    "Check if vat number unique in business partners when passed a second business partner with unique vat number - business partnership journey" in {
      val businessPartnerWithNonUniqueVatNumber = FormTestData.businessPartnerSoleProprietorWithVatNumber(FormTestData.vatNumber.value)
      val businessPartnersWithUniqueSecondVatNumber = FormTestData.partners.updated(0, businessPartnerWithNonUniqueVatNumber)
      val seqPages = journeys.partnershipPages map { page =>
        page.id match {
          case businessPartnersPage.id => page.asInstanceOf[RepeatingPage[BusinessPartner]] withData businessPartnersWithUniqueSecondVatNumber
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(businessPartnersPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.isVatNumberUniqueForBusinessPartner(
        businessPartnersWithUniqueSecondVatNumber.values.toList,
        index = 1
      ) shouldBe true
    }

    "Check if vat number unique in business partners when passed a second business partner with non-unique vat number - business partnership journey" in {
      val businessPartnerWithNonUniqueVatNumber = FormTestData.businessPartnerSoleProprietorWithVatNumber(FormTestData.vatNumber.value)
      val businessPartnersWithNonUniqueSecondVatNumber = FormTestData.partners.updated(1, businessPartnerWithNonUniqueVatNumber)
      val seqPages = journeys.partnershipPages map { page =>
        page.id match {
          case businessPartnersPage.id => page.asInstanceOf[RepeatingPage[BusinessPartner]] withData businessPartnersWithNonUniqueSecondVatNumber
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(businessPartnersPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.isVatNumberUniqueForBusinessPartner(
        businessPartnersWithNonUniqueSecondVatNumber.values.toList,
        index = 1
      ) shouldBe false
    }

    "Check if vat number unique in company officers when passed a first company officer with unique vat number - limited company journey" in {
      val companyOfficerWithNonUniqueVatNumber = FormTestData.companyOfficerCompanyWithVatNumber(FormTestData.vatNumber.value)
      val companyOfficersWithUniqueFirstVatNumber = FormTestData.companyOfficers.updated(1, companyOfficerWithNonUniqueVatNumber)
      val seqPages = journeys.limitedCompanyPages map { page =>
        page.id match {
          case companyOfficersPage.id =>
            page.asInstanceOf[RepeatingPage[CompanyOfficer]] withData companyOfficersWithUniqueFirstVatNumber
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(companyOfficersPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.isVatNumberUniqueForCompanyOfficer(
        companyOfficersWithUniqueFirstVatNumber.values.toList,
        index = 0
      ) shouldBe true
    }

    "Check if vat number unique in company officers when passed a first company officer with non-unique vat number - limited company journey" in {
      val companyOfficerWithNonUniqueVatNumber = FormTestData.companyOfficerCompanyWithVatNumber(FormTestData.vatNumber.value)
      val companyOfficersWithNonUniqueFirstVatNumber = FormTestData.companyOfficers.updated(0, companyOfficerWithNonUniqueVatNumber)
      val seqPages = journeys.limitedCompanyPages map { page =>
        page.id match {
          case companyOfficersPage.id =>
            page.asInstanceOf[RepeatingPage[CompanyOfficer]] withData companyOfficersWithNonUniqueFirstVatNumber
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(companyOfficersPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.isVatNumberUniqueForCompanyOfficer(
        companyOfficersWithNonUniqueFirstVatNumber.values.toList,
        index = 0
      ) shouldBe false
    }

    "Check if vat number unique in company officers when passed a second company officer with unique vat number - limited company journey" in {
      val companyOfficerWithNonUniqueVatNumber = FormTestData.companyOfficerCompanyWithVatNumber(FormTestData.vatNumber.value)
      val companyOfficersWithUniqueSecondVatNumber = FormTestData.companyOfficers.updated(0, companyOfficerWithNonUniqueVatNumber)
      val seqPages = journeys.limitedCompanyPages map { page =>
        page.id match {
          case companyOfficersPage.id =>
            page.asInstanceOf[RepeatingPage[CompanyOfficer]] withData companyOfficersWithUniqueSecondVatNumber
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(companyOfficersPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.isVatNumberUniqueForCompanyOfficer(
        companyOfficersWithUniqueSecondVatNumber.values.toList,
        index = 1
      ) shouldBe true
    }

    "Check if vat number unique in company officers when passed a second company officer with non-unique vat number - limited company journey" in {
      val companyOfficerWithNonUniqueVatNumber = FormTestData.companyOfficerCompanyWithVatNumber(FormTestData.vatNumber.value)
      val companyOfficersWithNonUniqueSecondVatNumber = FormTestData.companyOfficers.updated(1, companyOfficerWithNonUniqueVatNumber)
      val seqPages = journeys.limitedCompanyPages map { page =>
        page.id match {
          case companyOfficersPage.id =>
            page.asInstanceOf[RepeatingPage[CompanyOfficer]] withData companyOfficersWithNonUniqueSecondVatNumber
          case vatNumberPage.id => page.asInstanceOf[Page[VatNumber]] withData FormTestData.vatNumber
          case _ => page
        }
      }

      val request = journeyRequest(journeyPages = new JourneyPages(seqPages))
      val action = new PageAction(companyOfficersPage.id, None, journeys)(
        StubbedErrorHandler,
        scala.concurrent.ExecutionContext.Implicits.global
      )

      val refined = refinedRequest(action, request)
      refined.isVatNumberUniqueForCompanyOfficer(
        companyOfficersWithNonUniqueSecondVatNumber.values.toList,
        index = 1
      ) shouldBe false
    }

  }
}
