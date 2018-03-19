/*
 * Copyright 2018 HM Revenue & Customs
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

import uk.gov.hmrc.fhregistrationfrontend.models.des.{ChangeIndicators, Subscription}

object Diff {

  def changeIndicators(original: Subscription, amended: Subscription) =
    ChangeIndicators(
      original.organizationType != amended.organizationType,
      original.businessDetail != amended.businessDetail,
      original.businessDetail.partnership != amended.businessDetail.partnership,
      original.businessAddressForFHDDS.currentAddress != amended.businessAddressForFHDDS.currentAddress,
      original.businessAddressForFHDDS.previousOperationalAddress != amended.businessAddressForFHDDS.previousOperationalAddress,
      original.contactDetail != amended.contactDetail,
      original.GroupInformation != amended.GroupInformation,
      original.additionalBusinessInformation.partnerCorporateBody.flatMap(_.companyOfficials)
        != amended.additionalBusinessInformation.partnerCorporateBody.flatMap(_.companyOfficials),
      original.additionalBusinessInformation != amended.additionalBusinessInformation,
      original.additionalBusinessInformation.allOtherInformation.premises
        != amended.additionalBusinessInformation.allOtherInformation.premises,
      original.declaration != amended.declaration)

}
