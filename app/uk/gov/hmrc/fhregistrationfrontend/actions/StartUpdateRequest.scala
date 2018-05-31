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

package uk.gov.hmrc.fhregistrationfrontend.actions

import play.api.mvc.WrappedRequest
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.JourneyType.JourneyType

class StartUpdateRequest[A](
  val registrationNumber    : String,
  val currentJourneyType    : Option[JourneyType],
  request                   : UserRequest[A]
) extends WrappedRequest[A](request) {

  def userId: String = request.userId
  def email: Option[String] = request.ggEmail
}

