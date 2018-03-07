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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import javax.inject.{Inject, Singleton}

@Singleton
class ExternalUrls @Inject() (ds: CommonPlayDependencies) {

  val businessCustomerVerificationUrl = ds.conf
    .getString(s"${ds.env.mode}.microservice.services.business-customer-urls.business-verification")
    .getOrElse("http://localhost:9923/business-customer/FHDDS?backLinkUrl=http://localhost:1118/fhdds/continue")


}
