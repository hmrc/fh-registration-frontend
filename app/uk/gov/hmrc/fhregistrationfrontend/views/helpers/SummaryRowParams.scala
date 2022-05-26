/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
  * Created by ali on 20/02/18.
  *
  * for a single row in a summary, all vals are optional
  * meaning it's possible to leave any blank
  *
  * @param value     the value stored for this field
  * @param label     the label for the actual question
  * @param changeLink uri for change links
  * @param groupRow  Enum for position in a group of rows
  */
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow.GroupRow

case class SummaryRowParams(
  label: Option[String] = None,
  value: Option[String] = None,
  changeLink: Option[String] = None,
  groupRow: GroupRow = GroupRow.Single
)

object SummaryRowParams {

  def ofString(label: Option[String], value: String, changeLink: Option[String], groupRow: GroupRow): SummaryRowParams =
    SummaryRowParams(label, Some(value), changeLink, groupRow)

  def ofBoolean(
    label: Option[String],
    value: Option[Boolean],
    changeLink: Option[String],
    groupRow: GroupRow): SummaryRowParams =
    SummaryRowParams(label, value map (if (_) "Yes" else "No"), changeLink, groupRow)

  def ofBoolean(
    label: Option[String],
    value: Boolean,
    changeLink: Option[String],
    groupRow: GroupRow): SummaryRowParams =
    ofBoolean(label, Some(value), changeLink, groupRow)

  def ofDate(
    label: Option[String],
    value: Option[LocalDate],
    changeLink: Option[String],
    groupRow: GroupRow): SummaryRowParams =
    SummaryRowParams(label, value map (_.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))), changeLink, groupRow)

  def ofDate(
    label: Option[String],
    value: LocalDate,
    changeLink: Option[String],
    groupRow: GroupRow): SummaryRowParams =
    ofDate(label, Some(value), changeLink, groupRow)

}
