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

package uk.gov.hmrc.fhregistrationfrontend.forms.deregistration

import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.DeclarationForm.{alternativeEmailKey, ggEmailKey, isUseGgEmailKey}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{alternativeEmail, email}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{alternativeEmail, email, yesOrNo}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl.MappingsApi.{MappingOps, MappingWithKeyOps}

object ConfirmationForm {

  val confirmKey = "confirm"
  val isUseGgEmailKey = "isUseGgEmail"
  val ggEmailKey = "ggEmail"
  val alternativeEmailKey = "alternativeEmail"


  private val confirmMapping = confirmKey → yesOrNo
  private val isUseGgEmailMapping = isUseGgEmailKey → (yesOrNo onlyWhen (confirmMapping is true))
  private val ggEmailMapping = ggEmailKey → (email onlyWhen (isUseGgEmailMapping is Some(true)))
  private val alternativeEmailMapping = alternativeEmailKey → (alternativeEmail onlyWhen (isUseGgEmailMapping is Some(false)))


  val confirmationForm = Form(
    mapping(
      confirmMapping,
      isUseGgEmailMapping,
      ggEmailMapping,
      alternativeEmailMapping
    )(Confirmation.apply)(Confirmation.unapply)
  )
}
