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

case class ImportingActivitiesPage(
  mainPage: Page[Boolean],
  eoriNumberPage: Page[String],
  goodsPage: Page[Boolean],
  section: Option[String] = None
) extends Page[ImportingActivities] {

  override val id: String = "importingActivities"
  override val format: Format[ImportingActivities] = ImportingActivities.format

  val mainSection = Some("any")

  override def withData(data: ImportingActivities): Page[ImportingActivities] = {
    val newSection = if (data.hasEori) section else None
    val eoriNumberPageWithData =
      data.eoriNumber.map(_.eoriNumber).map(eori => eoriNumberPage withData eori).getOrElse(eoriNumberPage)
    val goodsPageWithData =
      data.eoriNumber.map(_.goodsImportedOutsideEori).map(goods => goodsPage withData goods).getOrElse(goodsPage)
    this copy (
      section = newSection,
      mainPage = mainPage withData data.hasEori,
      eoriNumberPage = eoriNumberPageWithData,
      goodsPage = goodsPageWithData
    )
  }

  override def parseFromRequest[X](withErrors: Rendering => X, withData: Page[ImportingActivities] => X)(
    implicit r: Request[_]): X =
    //    TODO: CASE MATCH
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
    //    TODO: CASE MATCH
    if (isMainSection && hasEori)
      Some("eoriNumber")
    else if (isMainSection && !hasEori)
      None
    else if (section.contains("eoriNumber"))
      Some("goods")
    else
//      TODO: PROB WRONG
      eoriNumberPage.nextSubsection

  override def previousSubsection: Option[String] =
    //    TODO: CASE MATCH
    if (isMainSection && hasEori)
      None
    else if (isMainSection && !hasEori)
      None
    else if (section.contains("goods"))
      Some("eoriNumber")
    else
      //      TODO: PROB WRONG
      eoriNumberPage.previousSubsection orElse mainSection

  private def isMainSection = section.isEmpty || (section == mainSection)
  private def hasEori = mainPage.data contains true

  override def render(
    bpr: BusinessRegistrationDetails,
    navigation: Navigation)(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
//    TODO: CASE MATCH
    if (isMainSection)
      mainPage.render(bpr, navigation)
    else if (section.contains("eoriNumber"))
      eoriNumberPage.render(bpr, navigation)
    else
      goodsPage.render(bpr, navigation)

  override val data: Option[ImportingActivities] = {
    //TODO: DATA NOT SAVING CORRECTLY - ROUTING IS FINE
    mainPage.data map { hasEori =>
//      val eori = eoriNumberPage.data
//      val goods = goodsPage.data
//      val eoriNumber: Option[EoriNumber] = for {
//        e <- eori
//        g <- goods
//      } yield EoriNumber(e, g)
//      TODO: BELOW WILL DEFAULT GOOD TO FALSE - TEMP HACK TO TEST SAVING/LOADING/ROUTING
      val eoriNumber: Option[EoriNumber] =
        eoriNumberPage.data.map(eori => EoriNumber(eori, goodsPage.data.getOrElse(false)))
      ImportingActivities(hasEori, eoriNumber)
    }
  }

  override def delete: Option[Page[ImportingActivities]] =
    if (isMainSection)
      None
    else
//      TODO: CORRECT THIS
      eoriNumberPage.delete map { updatedStoragePremisePage =>
        this copy (
          eoriNumberPage = updatedStoragePremisePage
        )
      }

//  TODO: CORRECT THIS
  override def pageStatus: PageStatus = {
    if (mainPage.pageStatus != Completed) mainPage.pageStatus
    else if (!hasEori) Completed
    else if (eoriNumberPage.pageStatus == Completed) Completed
    else InProgress
  }

  //  TODO: CORRECT THIS
  override def lastSection: Option[String] =
    if (hasEori)
      eoriNumberPage.lastSection
    else
      None

//  TODO: DO WE NEED THIS?
  override def updatedAddresses: List[Address] =
    if (isMainSection) mainPage.updatedAddresses
    else eoriNumberPage.updatedAddresses
}
