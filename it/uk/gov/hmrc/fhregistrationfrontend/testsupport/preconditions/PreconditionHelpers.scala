/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions

trait PreconditionHelpers {
  implicit val builder: PreconditionBuilder

  def commonPrecondition =
    builder.audit
      .writesAuditOrMerged()
      .user
      .isAuthorised()
      .businessCustomerFrontend
      .hasBusinessCustomerRecord
      .save4later
      .businessRecordWasSaved()
      .save4later
      .getNoneData()
      .fhddsBackend
      .hasNoEnrolmentProgress()

  def commonPreconditionWithMultipleAddressLookup(isSuccess: Boolean) =
    builder.audit
      .writesAuditOrMerged()
      .user
      .isAuthorised()
      .businessCustomerFrontend
      .hasBusinessCustomerRecord
      .fhddsBackend
      .hasNoEnrolmentProgress()
      .addressLookup
      .lookupAddress(isSuccess)

  def commonPreconditionWithSingleAddressLookup(isSuccess: Boolean) =
    builder.audit
      .writesAuditOrMerged()
      .user
      .isAuthorised()
      .businessCustomerFrontend
      .hasBusinessCustomerRecord
      .fhddsBackend
      .hasNoEnrolmentProgress()
      .addressLookup
      .lookupSingleAddress(isSuccess)

  def commonPreconditionWithEmptyAddressLookup(isSuccess: Boolean) =
    builder.audit
      .writesAuditOrMerged()
      .user
      .isAuthorised()
      .businessCustomerFrontend
      .hasBusinessCustomerRecord
      .fhddsBackend
      .hasNoEnrolmentProgress()
      .addressLookup
      .lookupNoAddress(isSuccess)

  def commonPreconditionAssist =
    builder.audit
      .writesAuditOrMerged()
      .user
      .isAuthorisedAssistant()
      .businessCustomerFrontend
      .hasBusinessCustomerRecord
      .save4later
      .businessRecordWasSaved()
      .save4later
      .getNoneData()
      .fhddsBackend
      .hasNoEnrolmentProgress()

  def commonPreconditionNoRole =
    builder.audit
      .writesAuditOrMerged()
      .user
      .isAuthorisedNoCredRole()
      .businessCustomerFrontend
      .hasBusinessCustomerRecord
      .save4later
      .businessRecordWasSaved()
      .save4later
      .getNoneData()
      .fhddsBackend
      .hasNoEnrolmentProgress()

  def withdrawalPrecondition =
    builder.audit.writesAuditOrMerged().user.isAuthorisedAndEnrolled

  def summaryPrecondition =
    builder.audit.writesAuditOrMerged().user.isAuthorised().save4later.hasFullFormData().save4later.acceptsDelete()

}
