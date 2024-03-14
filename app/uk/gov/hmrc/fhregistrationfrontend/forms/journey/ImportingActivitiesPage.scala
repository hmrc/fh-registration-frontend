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

//TODO: NEED TO CHANGE FORMAT OF IMPORTING ACTIVITIES TO BOOL, Option[String], Option[Bool] - WHAT IS THE BEST WAY
//ADD OPTION[STRING], ADD OPTION[BOOL] AND MIGRATE
case class ImportingActivitiesPage(
  mainPage: Page[Boolean],
  eoriNumberPage: Page[String],
  goodsPage: Page[Boolean],
  section: Option[String] = None,
  updatedAddresses: List[Address] = List.empty
) extends Page[ImportingActivities] {

  override val id: String = "importingActivities"
  override val format: Format[ImportingActivities] = ImportingActivities.format

  val mainSection = Some("any")
  val eoriNumberSection = Some("eoriNumber")
  val goodsSection = Some("goods")

  override def withData(data: ImportingActivities): Page[ImportingActivities] = {
    val newSection = if (data.hasEori) section else None
//    val eoriNumberPageWithData =
//      data.eoriNumber.map(_.eoriNumber).map(eori => eoriNumberPage withData eori).getOrElse(eoriNumberPage)
//    val goodsPageWithData =
//      data.eoriNumber.map(_.goodsImportedOutsideEori).map(goods => goodsPage withData goods).getOrElse(goodsPage)
    val eoriNumberPageWithData = data.eori.map(eori => eoriNumberPage withData eori) getOrElse eoriNumberPage
    val goodsPageWithData = data.goodsImported.map(goods => goodsPage withData goods) getOrElse goodsPage
    this copy (
      section = newSection,
      mainPage = mainPage withData data.hasEori,
      eoriNumberPage = eoriNumberPageWithData,
      goodsPage = goodsPageWithData
    )
  }

  override def parseFromRequest[X](withErrors: Rendering => X, withData: Page[ImportingActivities] => X)(
    implicit r: Request[_]): X =
    section match {
      case Some("eoriNumber") => {
        eoriNumberPage.parseFromRequest(
          withErrors,
          en => {
            val newValue = this copy (eoriNumberPage = en)
            withData(newValue)
          }
        )
      }
      case Some("goods") => {
        goodsPage.parseFromRequest(
          withErrors,
          goods => {
            val newValue = this copy (goodsPage = goods)
            withData(newValue)
          }
        )
      }
      case _ => {
        mainPage.parseFromRequest(
          withErrors,
          mp => {
            val newValue = this copy (mainPage = mp)
            withData(newValue)
          }
        )
      }
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
      eoriNumberSection
    else if (isMainSection && !hasEori)
      None
    else if (section == eoriNumberSection)
      goodsSection
    else if (section == goodsSection)
      None
    else
      None

  override def previousSubsection: Option[String] =
    if (isMainSection && hasEori)
      None
    else if (isMainSection && !hasEori)
      None
    else if (section == eoriNumberSection)
      mainSection
    else if (section == goodsSection)
      eoriNumberSection
    else
      None

  override def lastSection: Option[String] =
    if (hasEori)
      goodsSection
    else
      None

  private def isMainSection = section.isEmpty || (section == mainSection)
  private def hasEori = mainPage.data contains true

  override def render(
    bpr: BusinessRegistrationDetails,
    navigation: Navigation)(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
    section match {
      case Some("eoriNumber") => eoriNumberPage.render(bpr, navigation)
      case Some("goods")      => goodsPage.render(bpr, navigation)
      case _                  => mainPage.render(bpr, navigation)
    }

  override val data: Option[ImportingActivities] = {
    //TODO: DATA NOT SAVING CORRECTLY - ROUTING IS FINE
    mainPage.data map { hasEori =>
//      val eori = eoriNumberPage.data
//      val goods = goodsPage.data
//      val eoriNumber: Option[EoriNumber] = for {
//        e <- eori
//        g <- goods
//      } yield EoriNumber(e, g)
////      TODO: BELOW WILL DEFAULT GOOD TO FALSE - TEMP HACK TO TEST SAVING/LOADING/ROUTING
//      val eoriNumber: Option[EoriNumber] =
//        eoriNumberPage.data.map(eori => EoriNumber(eori, goodsPage.data.getOrElse(false)))
      ImportingActivities(hasEori, eori = eoriNumberPage.data, goodsImported = goodsPage.data)
    }
  }

  override def pageStatus: PageStatus =
    if (mainPage.pageStatus != Completed) mainPage.pageStatus
    else if (!hasEori) Completed
    else if (eoriNumberPage.pageStatus == Completed && goodsPage.pageStatus == Completed) Completed
    else InProgress

  override def delete: Option[Page[ImportingActivities]] = None
}
