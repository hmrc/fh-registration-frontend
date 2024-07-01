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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, VatNumber}
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails

case class VatNumberPage(
  mainPage: Page[VatNumber],
  section: Option[String] = None,
  updatedAddresses: List[Address] = List.empty
) extends Page[VatNumber] {

  override val id: String = "vatNumber"
  override val format: Format[VatNumber] = VatNumber.format

  val mainSection = Some("any")

  override def withData(data: VatNumber): Page[VatNumber] =
    this copy (mainPage = mainPage withData data)

  override def parseFromRequest[X](withErrors: Rendering => X, withData: Page[VatNumber] => X)(
    implicit r: Request[_]): X =
    section match {
      case _ =>
        mainPage.parseFromRequest(
          withErrors,
          mp => {
            val newValue = this copy (mainPage = mp)
            withData(newValue)
          }
        )
    }

  override val withSubsection: PartialFunction[Option[String], Page[VatNumber]] = {
    case None => this copy (section = mainSection)
  }
  override def nextSubsection: Option[String] = None
  override def previousSubsection: Option[String] = None
  override def lastSection: Option[String] = None

  override def render(
    bpr: BusinessRegistrationDetails,
    navigation: Navigation)(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
    section match {
      case _ => mainPage.render(bpr, navigation)
    }

  override val data: Option[VatNumber] = {
    mainPage.data map { vatNumber =>
      VatNumber(value = vatNumber.value, hasValue = true)
    }
  }

  override def pageStatus: PageStatus = if (data.isDefined) Completed else NotStarted

  override def delete: Option[Page[VatNumber]] = None
}
