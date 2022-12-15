/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{alternativeEmail, email, personName, personTitle, yesOrNo}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl.MappingsApi.{MappingOps, MappingWithKeyOps}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Declaration

object DeclarationForm {

  val fullNameKey = "fullName"
  val jobTitleKey = "jobTitle"
  val usingDefaultEmailKey = "usingDefaultEmail"
  val defaultEmailKey = "defaultEmail"
  val alternativeEmailKey = "alternativeEmail"

  private val usingDefaultEmailMapping = usingDefaultEmailKey -> yesOrNo
  private val defaultEmailMapping = defaultEmailKey           -> (email onlyWhen (usingDefaultEmailMapping is true))
  private val alternativeEmailMapping = alternativeEmailKey   -> (alternativeEmail onlyWhen (usingDefaultEmailMapping is false))

  def declarationForm = Form(
    mapping(
      fullNameKey -> personName,
      jobTitleKey -> personTitle,
      usingDefaultEmailMapping,
      defaultEmailMapping,
      alternativeEmailMapping
    )(Declaration.apply)(Declaration.unapply)
  )
}
