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

package uk.gov.hmrc.fhregistrationfrontend.views.companyOfficers.v2

import play.api.i18n.Messages

object CompanyOfficersHeaderWithCaption {

  def apply(title: String)(implicit messages: Messages) =
    s"""<header>
      <h1 class="govuk-heading-l">
        <span class="govuk-caption-l hmrc-caption-l">
          <span class="govuk-visually-hidden">${messages("fh.companyOfficers.screen-reader.section")}</span>
          ${messages("fh.companyOfficers.caption")}</span>
        $title
      </h1>
    </header>"""
}
