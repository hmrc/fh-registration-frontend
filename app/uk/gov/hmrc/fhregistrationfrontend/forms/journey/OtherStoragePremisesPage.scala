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

package uk.gov.hmrc.fhregistrationfrontend.forms.journey

import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Format
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, ListWithTrackedChanges, OtherStoragePremises, StoragePremise}
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails

case class OtherStoragePremisesPage(
  mainPage: Page[Boolean],
  storagePremisePage: Page[ListWithTrackedChanges[StoragePremise]],
  section: Option[String] = None
) extends Page[OtherStoragePremises] {

  override val id: String = "otherStoragePremises"
  override val format: Format[OtherStoragePremises] = OtherStoragePremises.format

  val mainSection = Some("any")

  override def withData(data: OtherStoragePremises): Page[OtherStoragePremises] = {
    val newSection = if (data.hasValue) section else None
    this copy (
      section = newSection,
      mainPage = mainPage withData data.hasValue,
      storagePremisePage = storagePremisePage withData data.value
    )
  }

  override def parseFromRequest[X](withErrors: Rendering => X, withData: Page[OtherStoragePremises] => X)(
    implicit r: Request[_]): X =
    if (isMainSection) {
      mainPage.parseFromRequest(
        withErrors,
        mp => {
          val newValue = this copy (mainPage = mp)
          withData(newValue)
        }
      )
    } else {
      storagePremisePage.parseFromRequest(
        withErrors,
        spp => {
          val newValue = this copy (storagePremisePage = spp)
          withData(newValue)
        }
      )
    }

  override val withSubsection: PartialFunction[Option[String], Page[OtherStoragePremises]] = {
    case None          => this copy (section = mainSection)
    case `mainSection` => this copy (section = mainSection)
    case newSection if hasOtherPremises && storagePremisePage.withSubsection.isDefinedAt(newSection) =>
      this copy (section = newSection,
      storagePremisePage = storagePremisePage withSubsection newSection)
  }

  override def nextSubsection: Option[String] =
    if (isMainSection && hasOtherPremises)
      Some("1")
    else if (isMainSection && !hasOtherPremises)
      None
    else
      storagePremisePage.nextSubsection

  override def previousSubsection: Option[String] =
    if (isMainSection) None
    else storagePremisePage.previousSubsection orElse mainSection

  private def isMainSection = section.isEmpty || (section == mainSection)
  private def hasOtherPremises = mainPage.data contains true

  override def render(bpr: BusinessRegistrationDetails, navigation: Navigation, formError: Option[FormError] = None)(
    implicit request: Request[_],
    messages: Messages,
    appConfig: AppConfig): Html =
    if (isMainSection)
      mainPage.render(bpr, navigation)
    else
      storagePremisePage.render(bpr, navigation)

  override val data: Option[OtherStoragePremises] = {
    mainPage.data map { hasOthers =>
      OtherStoragePremises(hasOthers, storagePremisePage.data getOrElse ListWithTrackedChanges.empty[StoragePremise]())
    }
  }

  override def delete: Option[Page[OtherStoragePremises]] =
    if (isMainSection)
      None
    else
      storagePremisePage.delete map { updatedStoragePremisePage =>
        this copy (
          storagePremisePage = updatedStoragePremisePage
        )
      }

  override def pageStatus: PageStatus =
    if (mainPage.pageStatus != Completed) mainPage.pageStatus
    else if (!hasOtherPremises) Completed
    else if (storagePremisePage.pageStatus == Completed) Completed
    else InProgress

  override def lastSection: Option[String] =
    if (hasOtherPremises)
      storagePremisePage.lastSection
    else
      None

  override def updatedAddresses: List[Address] =
    if (isMainSection) mainPage.updatedAddresses
    else storagePremisePage.updatedAddresses
}
