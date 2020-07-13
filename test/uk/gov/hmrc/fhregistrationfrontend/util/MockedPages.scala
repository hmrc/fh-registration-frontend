package uk.gov.hmrc.fhregistrationfrontend.util

import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page.NicholasPage
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{BasicPage, Journeys, OtherStoragePremisesPage, Page, RepeatingPage}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessCustomers, BusinessPartner, BusinessStatus, CompanyOfficer, CompanyRegistrationNumber, ContactPerson, DateOfIncorporation, ImportingActivities, MainBusinessAddress, NationalInsuranceNumber, TradingName, VatNumber}
import uk.gov.hmrc.fhregistrationfrontend.views.Views

trait MockedPages extends MockitoSugar {

  val mockViews: Views = mock[Views]
  val page: NicholasPage = new NicholasPage(mockViews)
  val mockJourneys: Journeys = mock[Journeys]
  val journeysWithMockViews: Journeys = new Journeys(mockViews)

  val contactPersonPage: Page[ContactPerson] = page.contactPersonPage
  val mainBusinessAddressPage: Page[MainBusinessAddress] = page.mainBusinessAddressPage
  val companyOfficersPage: RepeatingPage[CompanyOfficer] = page.companyOfficersPage
  val tradingNamePage: BasicPage[TradingName] = page.tradingNamePage
  val companyRegistrationNumberPage: Page[CompanyRegistrationNumber] = page.companyRegistrationNumberPage
  val dateOfIncorporationPage: BasicPage[DateOfIncorporation] = page.dateOfIncorporationPage
  val nationalInsuranceNumberPage: Page[NationalInsuranceNumber] = page.nationalInsuranceNumberPage
  val vatNumberPage: BasicPage[VatNumber] = page.vatNumberPage
  val businessPartnersPage: RepeatingPage[BusinessPartner] = page.businessPartnersPage
  val businessStatusPage: BasicPage[BusinessStatus] = page.businessStatusPage
  val businessCustomersPage: BasicPage[BusinessCustomers] = page.businessCustomersPage
  val importingActivitiesPage: BasicPage[ImportingActivities] = page.importingActivitiesPage
  val otherStoragePremisesPage: OtherStoragePremisesPage = page.otherStoragePremisesPage
}