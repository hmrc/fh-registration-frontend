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

import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, ContactPerson, LimitedCompanyApplication, MainBusinessAddress}
import uk.gov.hmrc.fhregistrationfrontend.models.des
import uk.gov.hmrc.fhregistrationfrontend.models.des.ContactDetail

class DesToForm {



//
  def limitedCompanySubmission(subscription: des.SubscriptionDisplay) = {
////    LimitedCompanyApplication(
////      mainBusinessAddress(subscription.businessAddressForFHDDS),
      contactPerson(subscription.contactDetail)
//    )
//      ???,
//      ???,
//      ???,
//      ???,
//      ???,
//      ???,
//      ???,
//      ???,
//      ???,
//      ???
//    )
  }

  def contactPerson(cd: des.ContactDetail) = ContactPerson(
    cd.names.firstName,
    cd.names.lastName,
    cd.title.getOrElse(""),
    cd.commonDetails.telephone.getOrElse(""),
    cd.commonDetails.email.getOrElse(""),
    !cd.usingSameContactAddress,
    ???,
    ???,
    ???
  )

  def mainBusinessAddress(mainAddress: des.BusinessAddressForFHDDS) = MainBusinessAddress(
    mainAddress.timeOperatedAtCurrentAddress,
    mainAddress.previousOperationalAddress map (_.anyPreviousOperatingAddress),
    mainAddress.previousOperationalAddress.flatMap(previousAddress),
    mainAddress.previousOperationalAddress.flatMap(previousAddressStartDate)
  )

  def previousAddressStartDate(pa:  des.PreviousOperationalAddress) = {
    for {
      prevAddressesDetail ← pa.previousOperationalAddressDetail
      prevAddressDetail ← prevAddressesDetail.headOption
    } yield {
      prevAddressDetail.previousAddressStartdate
    }
  }

  def previousAddress(pa:  des.PreviousOperationalAddress) = {
    for {
      prevAddressesDetail ← pa.previousOperationalAddressDetail
      prevAddressDetail ← prevAddressesDetail.headOption
    } yield {
      address(prevAddressDetail.previousAddress)
    }
  }

  def address(a: des.Address) = Address(
    a.line1,
    a.line2,
    a.line3,
    a.line4,
    a.postalCode.get,
    None
  )
}
