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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, InternationalAddress}
import uk.gov.hmrc.fhregistrationfrontend.views.html.helpers.SummaryAddressBlock
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

  def formatAddress(address: Address): String = {
    val optionalLines = List(address.addressLine2, address.addressLine3, address.addressLine4).flatten
      .map(line => s"<br>$line")
      .mkString("")
    s"${address.addressLine1}$optionalLines<br>${address.postcode}"
  }

  def formatAddress(address: InternationalAddress): String = {
    val optionalLines = List(address.addressLine2, address.addressLine3).flatten
      .map(line => s"<br>$line")
      .mkString("")
    s"${address.addressLine1}$optionalLines<br>${address.countryCode}"
  }

  def createAddressString(address: Address): String = {

    def convertOptionalAddressLineToString(optLine: Option[String]): String =
      optLine.fold[String]("")(line => s", $line")

    address.addressLine1 +
      convertOptionalAddressLineToString(address.addressLine2) +
      convertOptionalAddressLineToString(address.addressLine3) +
      convertOptionalAddressLineToString(address.addressLine4) + s", ${address.postcode}"
  }

  def createSummaryRow(params: SummaryRowParams, summaryActions: Option[Actions]): SummaryListRow =
    SummaryListRow(
      key = Key(
        content = params.label.fold[Content](Empty)(label => HtmlContent(label))
      ),
      value = Value(
        content = params.value.fold[Content](Empty)(label => HtmlContent(label))
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
          )
        )
      )
    } else None
}
