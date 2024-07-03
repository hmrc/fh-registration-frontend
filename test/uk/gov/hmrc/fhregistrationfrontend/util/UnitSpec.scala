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

package uk.gov.hmrc.fhregistrationfrontend.util

import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{BasicPage, Journeys, Page}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{ContactPerson, MainBusinessAddress, TradingName, VatNumber}
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.ExecutionContext
import scala.language.{implicitConversions, postfixOps}

trait UnitSpec extends AnyWordSpecLike with Matchers with OptionValues with GuiceOneAppPerSuite {

  lazy val views: Views = app.injector.instanceOf[Views]
  lazy val journeys: Journeys = new Journeys(views)
  lazy val injectedJourneys = app.injector.instanceOf[Journeys]

  lazy val page: Page.InjectedPage = app.injector.instanceOf[Page.InjectedPage]
  lazy val mainBusinessAddressPage: Page[MainBusinessAddress] = page.mainBusinessAddressPage
  lazy val contactPersonPage: Page[ContactPerson] = page.contactPersonPage
  lazy val tradingNamePage: BasicPage[TradingName] = page.tradingNamePage
  lazy val businessPartnersPage = page.businessPartnersPage
  lazy val vatNumberPage: BasicPage[VatNumber] = page.vatNumberPage
  lazy val companyRegistrationNumberPage = page.companyRegistrationNumberPage
  lazy val dateOfIncorporationPage = page.dateOfIncorporationPage
  lazy val nationalInsuranceNumberPage = page.nationalInsuranceNumberPage
  lazy val companyOfficersPage = page.companyOfficersPage
  lazy val businessStatusPage = page.businessStatusPage
  lazy val importingActivitiesPage = page.importingActivitiesPage
  lazy val businessCustomersPage = page.businessCustomersPage
  lazy val otherStoragePremisesPage = page.otherStoragePremisesPage

  implicit val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

}
