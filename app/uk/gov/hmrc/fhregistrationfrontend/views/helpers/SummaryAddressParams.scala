/*
 * Copyright 2021 HM Revenue & Customs
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

import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow
import uk.gov.hmrc.fhregistrationfrontend.views.summary.GroupRow.GroupRow

/*
 * for a single row in a summary
 *
 * @param value     the value stored for this field
 * @param label     the label for the actual question
 * @param changeLink  uri for change links
 * @param groupRow position in a summary group of rows
 */

case class SummaryAddressParams(
  label: Option[String] = None,
  value: Html,
  changeLink: Option[String] = None,
  groupRow: GroupRow = GroupRow.Single
)
