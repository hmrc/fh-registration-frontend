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

import play.api.i18n.Messages
import play.api.libs.json.Format
import play.api.mvc.Request
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, EoriNumber, ImportingActivities}
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails

case class NewImportingActivitiesPage(
  mainPage: Page[Boolean],
  eoriNumberPage: Page[String],
  goodsPage: Page[Boolean],
  section: Option[String] = None
) extends Page[ImportingActivities] {

  override val id: String = "importingActivities"
  override val format: Format[ImportingActivities] = ImportingActivities.format

  val mainSection = Some("any")
//TODO: DATA NOT SAVING CORRECTLY - ROUTING IS FINE
  override def withData(data: ImportingActivities): Page[ImportingActivities] = {
    val newSection = if (data.hasEori) section else None
    this copy (
      section = newSection,
      mainPage = mainPage withData data.hasEori,
//      eoriNumberPage = if (data.hasEori) eoriNumberPage withData data.eoriNumber.get else eoriNumberPage
      eoriNumberPage = eoriNumberPage withData data.eoriNumber
        .map(_.eoriNumber)
        .getOrElse(""),
      goodsPage = goodsPage withData data.eoriNumber.map(_.goodsImportedOutsideEori).getOrElse(false)
    )
  }

  override def parseFromRequest[X](withErrors: Rendering => X, withData: Page[ImportingActivities] => X)(
    implicit r: Request[_]): X =
    if (isMainSection) {
      mainPage.parseFromRequest(
        withErrors,
        mp => {
          val newValue = this copy (mainPage = mp)
          withData(newValue)
        }
      )
    } else if (section.contains("eoriNumber")) {
      eoriNumberPage.parseFromRequest(
        withErrors,
        spp => {
          val newValue = this copy (eoriNumberPage = spp)
          withData(newValue)
        }
      )
    } else {
      goodsPage.parseFromRequest(
        withErrors,
        spp => {
          val newValue = this copy (goodsPage = spp)
          withData(newValue)
        }
      )
    }

  override val withSubsection: PartialFunction[Option[String], Page[ImportingActivities]] = {
    case None          => this copy (section = mainSection)
    case `mainSection` => this copy (section = mainSection)
    case newSection if hasEori =>
      this copy (section = newSection,
      eoriNumberPage = eoriNumberPage,
      goodsPage = goodsPage)
  }

  override def nextSubsection: Option[String] =
    if (isMainSection && hasEori)
      Some("eoriNumber")
    else if (isMainSection && !hasEori)
      None
    else if (section.contains("eoriNumber"))
      Some("goods")
    else
      eoriNumberPage.nextSubsection

  override def previousSubsection: Option[String] =
    if (isMainSection) None
    else eoriNumberPage.previousSubsection orElse mainSection

  private def isMainSection = section.isEmpty || (section == mainSection)
  private def hasEori = mainPage.data contains true

  override def render(
    bpr: BusinessRegistrationDetails,
    navigation: Navigation)(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
    if (isMainSection)
      mainPage.render(bpr, navigation)
    else if (section.contains("eoriNumber"))
      eoriNumberPage.render(bpr, navigation)
    else
      goodsPage.render(bpr, navigation)

  override val data: Option[ImportingActivities] = {
    mainPage.data map { hasEori =>
      ImportingActivities(hasEori, None)
    }
  }

  override def delete: Option[Page[ImportingActivities]] =
    if (isMainSection)
      None
    else
      eoriNumberPage.delete map { updatedStoragePremisePage =>
        this copy (
          eoriNumberPage = updatedStoragePremisePage
        )
      }

  override def pageStatus: PageStatus =
    if (mainPage.pageStatus != Completed) mainPage.pageStatus
    else if (!hasEori) Completed
    else if (eoriNumberPage.pageStatus == Completed) Completed
    else InProgress

  override def lastSection: Option[String] =
    if (hasEori)
      eoriNumberPage.lastSection
    else
      None

  override def updatedAddresses: List[Address] =
    if (isMainSection) mainPage.updatedAddresses
    else eoriNumberPage.updatedAddresses
}
