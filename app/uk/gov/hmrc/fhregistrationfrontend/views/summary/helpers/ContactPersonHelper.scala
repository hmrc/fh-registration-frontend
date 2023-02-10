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

package uk.gov.hmrc.fhregistrationfrontend.views.summary.helpers

import play.api.i18n.Messages
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{ContactPerson => ContactPersonModel}
import uk.gov.hmrc.fhregistrationfrontend.views.html.helpers._
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.ViewHelpers
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.Helpers
import uk.gov.hmrc.fhregistrationfrontend.views.summary._
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.SummaryRowParams

object ContactPersonHelper {
  def apply(contactPersonForm: ContactPersonModel, bpr: BusinessRegistrationDetails, mode: Mode)(
    implicit messages: Messages) = {

    val ContactPersonAddressLabel =
      if (!contactPersonForm.usingSameContactAddress) {
        if (contactPersonForm.ukOtherAddress.contains(true)) {
          "fh.contact_person.contact_address_new.label"
        } else {
          "fh.contact_person.contact_address_international.label"
        }
      } else "fh.contact_person.contact_address.title"

    val ContactPersonAddress =
      if (contactPersonForm.otherUkContactAddress.isDefined) {
        Helpers.formatAddress(contactPersonForm.otherUkContactAddress.get)
      } else {
        ""
      }

    val PageLabel =
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.generic.name")),
          contactPersonForm.firstName + " " + contactPersonForm.lastName,
          None,
          GroupRow.Top),
        Helpers.createChangeLink(
          Mode isEditable mode,
          "form/contactPerson",
          Text("Change"),
          Some(Messages("fh.generic.name"))
        )
      )

    val JobTitle =
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.contact_person.job_title.label")),
          contactPersonForm.jobTitle,
          None,
          GroupRow.Top),
        None)

    val telephoneNumber =
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.contact_person.telephone.label")),
          contactPersonForm.telephone,
          None,
          GroupRow.Top),
        None)

    val Address =
      if (contactPersonForm.otherUkContactAddress.isDefined) {
        Seq(
          Helpers.createSummaryRow(
            SummaryRowParams
              .ofString(Some(Messages(ContactPersonAddressLabel)), ContactPersonAddress, None, GroupRow.Bottom),
            None))
      } else Seq.empty

    if (Address.nonEmpty) {
      Seq(
        PageLabel,
        JobTitle,
        telephoneNumber
      ) ++ Address
    } else {
      Seq(
        PageLabel,
        JobTitle,
        telephoneNumber
      )
    }
  }
}
