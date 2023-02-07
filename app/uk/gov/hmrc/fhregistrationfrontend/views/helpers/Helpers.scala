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

package uk.gov.hmrc.fhregistrationfrontend.views.helpers

import java.text.SimpleDateFormat
import java.util.Date
import play.api.data.FormError
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address
import uk.gov.hmrc.govukfrontend.views.html.components._

object Helpers {
  def getError(error: Option[FormError]): String =
    if (error.nonEmpty) error.head.message
    else ""

  def formatTimestamp(date: Option[Date]): String =
    date.map(d => formatTimestamp(d)).getOrElse("")

  def formatTimestamp(date: Date): String =
    new SimpleDateFormat("dd MMMM yyyy HH:mm").format(date)

  def formatDate(date: Date): String =
    new SimpleDateFormat("dd MMMM yyyy").format(date)

  def findContactPersonAddressLabel(usingSameContactAddress: Boolean, ukOtherAddress: Boolean): String =
    if (!usingSameContactAddress) {
      if (ukOtherAddress) {
        "fh.contact_person.contact_address_new.label"
      } else {
        "fh.contact_person.contact_address_international.label"
      }
    } else {
      "fh.contact_person.contact_address.title"
    }

  def formatAddress(address: Address): String =
    s"""${address.addressLine1}${address.addressLine2}${address.addressLine3}${address.addressLine4}<br>${address.postcode}"""

  def findAddress(optAddress: Option[Address]): String = {
    def getAddressString(opt: Option[String]): String =
      opt match {
        case Some(string) => s"<br> $string"
        case None         => ""
      }

    if (optAddress.isDefined) {
      val add = optAddress.get
      s"""${add.addressLine1}${getAddressString(add.addressLine2)}${getAddressString(add.addressLine3)}${getAddressString(
        add.addressLine4)}<br>${add.postcode}"""
    } else {
      "" // TODO: Change implementation later (not ideal?)
    }
  }

  def createSummaryRow(params: SummaryRowParams, summaryActions: Option[Actions]): SummaryListRow =
    SummaryListRow(
      key = Key(
        content = if (params.label.isDefined) {
          HtmlContent(params.label.get)
        } else {
          Empty // TODO: Change implementation later (not ideal?)
        }
      ),
      value = Value(
        content = if (params.value.isDefined) {
          HtmlContent(params.value.get)
        } else {
          Empty // TODO: Change implementation later (not ideal?)
        }
      ),
      actions = summaryActions
    )

  def createChangeLink(isEditable: Boolean, href: String, content: Content, hiddenText: Some[String]) =
    if (isEditable) {
      Some(
        Actions(
          items = Seq(
            ActionItem(
              href = href,
              content = content,
              visuallyHiddenText = hiddenText
            )
          )))
    } else None

  def checkForNone(value: Option[String]) =
    if (value.isDefined) {
      value.get
    } else None
}
