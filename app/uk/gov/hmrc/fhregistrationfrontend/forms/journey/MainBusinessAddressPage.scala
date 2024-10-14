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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, MainBusinessAddress, PreviousAddress}
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails

case class MainBusinessAddressPage(
  mainPage: Page[String],
  hasPreviousAddressPage: Page[Boolean],
  previousAddressPage: Page[PreviousAddress],
  section: Option[String] = None,
  updatedAddresses: List[Address] = List.empty
) extends Page[MainBusinessAddress] {

  override val id: String = "mainBusinessAddress"
  override val format: Format[MainBusinessAddress] = MainBusinessAddress.format

  val mainSection = Some("years-at-current-address")
  val anyPreviousBusinessAddressSection = Some("any-previous-business-address")
  val previousBusinessAddressSection = Some("previous-business-address")

  override def withData(data: MainBusinessAddress): Page[MainBusinessAddress] = {
    val newSection =
      if (data.timeAtCurrentAddress == MainBusinessAddress.TimeAtCurrentAddressOptions.head) section else None
    val hasPreviousAddressPageWithData =
      data.hasPreviousAddress.map(hpa => hasPreviousAddressPage withData hpa) getOrElse hasPreviousAddressPage
    val previousAddressPageData = for {
      previousAddress          <- data.previousAddress
      previousAddressStartDate <- data.previousAddressStartdate
    } yield PreviousAddress(previousAddress, previousAddressStartDate)
    val previousAddressPageWithData =
      previousAddressPageData.map(pa => previousAddressPage withData pa) getOrElse previousAddressPage
    this copy (
      section = newSection,
      mainPage = mainPage withData data.timeAtCurrentAddress,
      hasPreviousAddressPage = hasPreviousAddressPageWithData,
      previousAddressPage = previousAddressPageWithData
    )
  }

  override def parseFromRequest[X](withErrors: Rendering => X, withData: Page[MainBusinessAddress] => X)(implicit
    r: Request[_]
  ): X =
    section match {
      case Some("any-previous-business-address") =>
        hasPreviousAddressPage.parseFromRequest(
          withErrors,
          hpa => {
            val newValue = this copy (hasPreviousAddressPage = hpa)
            withData(newValue)
          }
        )
      case Some("previous-business-address") =>
        previousAddressPage.parseFromRequest(
          withErrors,
          pa => {
            val newValue = this copy (previousAddressPage = pa)
            withData(newValue)
          }
        )
      case _ =>
        mainPage.parseFromRequest(
          withErrors,
          mp => {
            val newValue = this copy (mainPage = mp)
            withData(newValue)
          }
        )
    }

  override val withSubsection: PartialFunction[Option[String], Page[MainBusinessAddress]] = {
    case None          => this copy (section = mainSection)
    case `mainSection` => this copy (section = mainSection)
    case newSection if atCurrentAddressLessThan3Years && hasPreviousAddress =>
      this copy (section = newSection,
      hasPreviousAddressPage = hasPreviousAddressPage,
      previousAddressPage = previousAddressPage)
    case newSection if atCurrentAddressLessThan3Years && !hasPreviousAddress =>
      this copy (section = newSection,
      hasPreviousAddressPage = hasPreviousAddressPage)
  }

  override def nextSubsection: Option[String] =
    if (isMainSection && atCurrentAddressLessThan3Years)
      anyPreviousBusinessAddressSection
    else if (isMainSection && !atCurrentAddressLessThan3Years)
      None
    else if (section == anyPreviousBusinessAddressSection && hasPreviousAddress)
      previousBusinessAddressSection
    else if (section == anyPreviousBusinessAddressSection && !hasPreviousAddress)
      None
    else if (section == previousBusinessAddressSection)
      None
    else
      None

  override def previousSubsection: Option[String] =
    if (isMainSection && atCurrentAddressLessThan3Years)
      None
    else if (isMainSection && !atCurrentAddressLessThan3Years)
      None
    else if (section == anyPreviousBusinessAddressSection)
      mainSection
    else if (section == previousBusinessAddressSection)
      anyPreviousBusinessAddressSection
    else
      None

  override def lastSection: Option[String] =
    if (atCurrentAddressLessThan3Years && hasPreviousAddress)
      previousBusinessAddressSection
    else if (atCurrentAddressLessThan3Years && !hasPreviousAddress)
      anyPreviousBusinessAddressSection
    else
      None

  private def isMainSection = section.isEmpty || (section == mainSection)
  private def atCurrentAddressLessThan3Years =
    mainPage.data contains MainBusinessAddress.TimeAtCurrentAddressOptions.head
  private def hasPreviousAddress = hasPreviousAddressPage.data.contains(true)

  override def render(bpr: BusinessRegistrationDetails, navigation: Navigation)(implicit
    request: Request[_],
    messages: Messages,
    appConfig: AppConfig
  ): Html =
    section match {
      case Some("any-previous-business-address") => hasPreviousAddressPage.render(bpr, navigation)
      case Some("previous-business-address")     => previousAddressPage.render(bpr, navigation)
      case _                                     => mainPage.render(bpr, navigation)
    }

  override val data: Option[MainBusinessAddress] =
    mainPage.data map { timeAtCurrentAddress =>
      MainBusinessAddress(
        timeAtCurrentAddress,
        hasPreviousAddress = hasPreviousAddressPage.data,
        previousAddress = previousAddressPage.data.map(_.address),
        previousAddressStartdate = previousAddressPage.data.map(_.startDate)
      )
    }

  override def pageStatus: PageStatus =
    if (mainPage.pageStatus != Completed) mainPage.pageStatus
    else if (!atCurrentAddressLessThan3Years) Completed
    else if (hasPreviousAddressPage.pageStatus == Completed && !hasPreviousAddress) Completed
    else if (
      hasPreviousAddressPage.pageStatus == Completed && hasPreviousAddress && previousAddressPage.pageStatus == Completed
    ) Completed
    else InProgress

  override def delete: Option[Page[MainBusinessAddress]] = None
}
