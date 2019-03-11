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

package uk.gov.hmrc.fhregistrationfrontend.models.submissiontracking

import play.api.libs.json.Json
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress.EnrolmentProgress

case class SubmissionTracking(
                               userId: String,
                               formBundleId: String,
                               email: String,
                               submissionTime: Long,
                               enrolmentProgressOpt: Option[EnrolmentProgress],
                               registrationNumber: Option[String]
                             ) {
  def enrolmentProgress = enrolmentProgressOpt getOrElse EnrolmentProgress.Pending
}

object SubmissionTracking {
  implicit val formats = Json.format[SubmissionTracking]

  def apply(
             userId: String,
             formBundleId: String,
             email: String,
             submissionTime: Long,
             enrolmentProgress: EnrolmentProgress,
             registrationNumber: String
           ): SubmissionTracking = SubmissionTracking(
    userId,
    formBundleId,
    email,
    submissionTime,
    Some(enrolmentProgress),
    Some(registrationNumber))



  val UserIdField = "userId"
  val FormBundleIdField = "formBundleId"
  val EnrolmentProgressField = "enrolmentProgressOpt"
  val RegistrationNumberField = "registrationNumber"


}
