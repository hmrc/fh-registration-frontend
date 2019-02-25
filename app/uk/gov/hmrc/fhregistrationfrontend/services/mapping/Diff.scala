/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.services.mapping

import monocle.macros.GenLens
import monocle.Monocle.some
import monocle.Optional
import uk.gov.hmrc.fhregistrationfrontend.models.des._

object Diff {

  def changeIndicators(original: Subscription, amended: Subscription) =
    ChangeIndicators(
      businessTypeChanged(original, amended),
      businessDetailChanged(original, amended),
      partnersChanged(original, amended),
      original.businessAddressForFHDDS != amended.businessAddressForFHDDS,
      original.businessAddressForFHDDS.previousOperationalAddress != amended.businessAddressForFHDDS.previousOperationalAddress,
      original.contactDetail != amended.contactDetail,
      groupMemberChanged(original, amended),
      original.additionalBusinessInformation.partnerCorporateBody != amended.additionalBusinessInformation.partnerCorporateBody,
      additionalBusinessInformationChanged(original.additionalBusinessInformation.allOtherInformation, amended.additionalBusinessInformation.allOtherInformation),
      original.additionalBusinessInformation.allOtherInformation.premises
        != amended.additionalBusinessInformation.allOtherInformation.premises,
      original.declaration != amended.declaration)

  import Lenses._

  private def businessTypeChanged(original: Subscription, amended: Subscription) = {
    anyChanged(original, amended, organizationType, fhBusinessDetail)
  }

  private def groupMemberChanged(original: Subscription, amended: Subscription) = {
    anyChanged(original, amended)
  }

  private def businessDetailChanged(original: Subscription, amended: Subscription) = {
    anyChanged(original, amended,
      soleProprietor, nonProprietor, limitedLiabilityPartnershipCorporateBody
    )
  }

  private def partnersChanged(original: Subscription, amended: Subscription) = {
    anyChanged(original, amended, partnership)
  }

  private def anyChanged(original: Subscription, amended: Subscription, lenses: Optional[Subscription, _]*) =
    lenses
      .iterator
      .exists { lens ⇒ lens.getOption(original) != lens.getOption(amended) }

  private def additionalBusinessInformationChanged(original: AllOtherInformation, amended: AllOtherInformation) = {
    original.numberOfCustomers != amended.numberOfCustomers ||
      original.doesEORIExist != amended.doesEORIExist ||
      original.EORINumber != amended.EORINumber
  }



  object Lenses {
    val organizationType = GenLens[Subscription](_.organizationType).asOptional
    val fhBusinessDetail = GenLens[Subscription](_.FHbusinessDetail).asOptional

    //val groupInformation = GenLens[Subscription](_.GroupInformation) composePrism some
    //val creatingFHDDSGroup = groupInformation
     // .composeLens(GenLens[LimitedLiabilityOrCorporateBodyWithOutGroup](_.creatingFHDDSGroup))

    //val confirmationByRepresentative = groupInformation
    //  .composeLens(GenLens[LimitedLiabilityOrCorporateBodyWithOutGroup](_.confirmationByRepresentative))


    //val groupMemberDetail = groupInformation
     // .composeLens(GenLens[LimitedLiabilityOrCorporateBodyWithOutGroup](_.groupMemberDetail))
      //.composePrism(some)

    val numberOfCustomers = GenLens[Subscription](_.additionalBusinessInformation.allOtherInformation.numberOfCustomers)

    val soleProprietor = GenLens[Subscription](_.businessDetail.soleProprietor) composePrism some
    val nonProprietor = GenLens[Subscription](_.businessDetail.nonProprietor) composePrism some
    val limitedLiabilityPartnershipCorporateBody = GenLens[Subscription](_.businessDetail.limitedLiabilityPartnershipCorporateBody) composePrism some

    val partnership = GenLens[Subscription](_.businessDetail.partnership) composePrism some
  }

}
