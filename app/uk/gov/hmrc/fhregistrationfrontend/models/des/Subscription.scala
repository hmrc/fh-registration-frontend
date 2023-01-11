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

package uk.gov.hmrc.fhregistrationfrontend.models.des

import play.api.libs.json.Json

case class Subscription(
  organizationType: String,
  FHbusinessDetail: IsNewFulfilmentBusiness,
  additionalBusinessInformation: AdditionalBusinessInformationwithType,
  businessDetail: BusinessDetail,
  businessAddressForFHDDS: BusinessAddressForFHDDS,
  contactDetail: ContactDetail,
  declaration: Declaration)

object Subscription {
  implicit val format = Json.format[Subscription]

  def of(sd: SubscriptionDisplay) =
    Subscription(
      sd.organizationType,
      sd.FHbusinessDetail,
      sd.additionalBusinessInformation,
      sd.businessDetail,
      sd.businessAddressForFHDDS,
      sd.contactDetail,
      sd.declaration
    )
}

case class SubScriptionCreate(
  requestType: String,
  subScriptionCreate: Subscription,
  changeIndicators: Option[ChangeIndicators]
)

object SubScriptionCreate {
  implicit val format = Json.format[SubScriptionCreate]

  def apply(subscription: Subscription): SubScriptionCreate =
    SubScriptionCreate("Create", subscription, None)

  def subscriptionAmend(changeIndicators: ChangeIndicators, subscription: Subscription): SubScriptionCreate =
    SubScriptionCreate("Update", subscription, Some(changeIndicators))

}
