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
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.govukfrontend.views.html.components.*
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.Helpers
import uk.gov.hmrc.fhregistrationfrontend.views.summary.*
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.SummaryRowParams
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address

object ContactPersonHelper {
  def apply(contactPersonForm: ContactPersonModel, bpr: BusinessRegistrationDetails, mode: Mode)(implicit
    messages: Messages
  ) = {
    val PageLabel =
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.generic.name")),
          contactPersonForm.firstName + " " + contactPersonForm.lastName,
          None,
          GroupRow.Top
        ),
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
          GroupRow.Top
        ),
        Helpers.createChangeLink(
          Mode isEditable mode,
          "form/contactPerson",
          Text("Change"),
          Some(Messages("fh.contact_person.job_title.label"))
        )
      )

    val TelephoneNumber =
      Helpers.createSummaryRow(
        SummaryRowParams.ofString(
          Some(Messages("fh.contact_person.telephone.label")),
          contactPersonForm.telephone,
          None,
          GroupRow.Top
        ),
        Helpers.createChangeLink(
          Mode isEditable mode,
          "form/contactPerson",
          Text("Change"),
          Some(Messages("fh.contact_person.telephone.label"))
        )
      )

    val ContactPersonAddressLabel = (contactPersonForm.ukOtherAddress, bpr.businessAddress.country) match {
      case (Some(true), _) =>
        "fh.contact_person.contact_address_new.label"
      case (Some(false), _) =>
        "fh.contact_person.contact_address_international.label"
      case (None, "GB") =>
        "fh.contact_person.contact_address_new.label"
      case (None, _) =>
        "fh.contact_person.contact_address_international.label"
    }

    val ContactPersonAddress: String =
      (
        contactPersonForm.otherUkContactAddress,
        contactPersonForm.otherInternationalContactAddress,
        bpr.businessAddress
      ) match {

        case (Some(otherUkContactAddress), None, _) =>
          Helpers.formatAddress(otherUkContactAddress)

        case (None, Some(otherInternationalContactAddress), _) =>
          Helpers.formatAddress(otherInternationalContactAddress)

        case (None, None, businessAddress) =>
          Helpers.formatBusinessRegistrationAddress(businessAddress)
        case _ =>
          Helpers.formatBusinessRegistrationAddress(bpr.businessAddress)
      }

    val BusinessAddress =
      Seq(
        Helpers.createSummaryRow(
          SummaryRowParams
            .ofString(Some(Messages(ContactPersonAddressLabel)), ContactPersonAddress, None, GroupRow.Bottom),
          Helpers.createChangeLink(
            Mode isEditable mode,
            "form/contactPerson",
            Text("Change"),
            Some(Messages(ContactPersonAddressLabel))
          )
        )
      )

    Seq(
      PageLabel,
      JobTitle,
      TelephoneNumber
    ) ++ BusinessAddress
  }
}
