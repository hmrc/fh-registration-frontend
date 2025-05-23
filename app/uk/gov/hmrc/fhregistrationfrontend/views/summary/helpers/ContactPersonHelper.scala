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
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.{Address, BusinessRegistrationDetails}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{ContactPerson => ContactPersonModel}
import uk.gov.hmrc.fhregistrationfrontend.views.Mode
import uk.gov.hmrc.fhregistrationfrontend.views.Mode.Mode
import uk.gov.hmrc.govukfrontend.views.html.components.*
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.Helpers
import uk.gov.hmrc.fhregistrationfrontend.views.summary.*
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.SummaryRowParams
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address => FormAddress}

import java.util.regex.Pattern

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
    val ContactPersonAddressLabel =
      if (contactPersonForm.ukOtherAddress.contains(false)) {
        "fh.contact_person.contact_address_international.label"
      } else {
        "fh.contact_person.contact_address_new.label"
      }

    object ConverterBusinessAddress {
      def toFormAddress(b: Address): FormAddress =
        FormAddress(
          addressLine1 = b.line1,
          addressLine2 = Some(b.line2),
          addressLine3 = b.line3,
          addressLine4 = b.line4,
          postcode = b.postcode.getOrElse(""),
          countryCode = Some(b.country),
          lookupId = None
        )
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
          Helpers.formatAddress(ConverterBusinessAddress.toFormAddress(businessAddress))
      }

    val Address =
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
    ) ++ Address
  }
}
