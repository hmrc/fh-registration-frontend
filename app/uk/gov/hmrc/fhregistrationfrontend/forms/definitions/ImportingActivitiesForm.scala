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

package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{eoriNumber, yesOrNo}
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.dsl.MappingsApi.{MappingOps, MappingWithKeyOps}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{EoriNumber, ImportingActivities}
object ImportingActivitiesForm {

  val eoriNumberKey = "eoriNumber"
  val goodsImportedOutsideEoriKey = "goodsImportedOutsideEori"
  val hasEoriKey = "hasEori"

  val eoriNumberMapping = mapping(
    eoriNumberKey               -> eoriNumber,
    goodsImportedOutsideEoriKey -> yesOrNo()
  )(EoriNumber.apply)(EoriNumber.unapply)

  val hasEoriMapping = hasEoriKey               -> yesOrNo()
  val optionalEoriNumberMapping = eoriNumberKey -> (eoriNumberMapping onlyWhen (hasEoriMapping is true))

  val hasEoriForm = Form(hasEoriMapping)
  val eoriNumberForm: Form[EoriNumber] = Form(eoriNumberMapping)

//  TODO: INVESTIGATE - DO I NEED TO ALTER THIS?
  val optionalEoriNumberOnlyMapping = eoriNumberKey                             -> (eoriNumber onlyWhen (hasEoriMapping is true))
  val optionalGoodsImportedOutsideEoriOnlyMapping = goodsImportedOutsideEoriKey -> (yesOrNo() onlyWhen (hasEoriMapping is true))
  //
  //  val eoriNumberOnlyForm: Form[Option[String]] = Form(optionalEoriNumberOnlyMapping)
  //  val goodsImportedOutsideEoriOnlyForm: Form[Option[Boolean]] = Form(optionalGoodsImportedOutsideEoriOnlyMapping)

  val eoriNumberOnlyMapping = eoriNumberKey                             -> eoriNumber
  val goodsImportedOutsideEoriOnlyMapping = goodsImportedOutsideEoriKey -> yesOrNo()

  val eoriNumberOnlyForm: Form[String] = Form(eoriNumberOnlyMapping)
  val goodsImportedOutsideEoriOnlyForm: Form[Boolean] = Form(goodsImportedOutsideEoriOnlyMapping)

  val importingActivitiesForm: Form[ImportingActivities] = {
    val apply: (Boolean, Option[EoriNumber]) => ImportingActivities = (hasEori, eoriNumber) =>
      ImportingActivities(hasEori, eoriNumber)
    val unapply: ImportingActivities => Option[(Boolean, Option[EoriNumber])] =
      importingActivities => {
        Some((importingActivities.hasEori, importingActivities.eoriNumber))
      }
    Form(
      mapping(
        hasEoriMapping,
        optionalEoriNumberMapping
      )(apply)(unapply)
    )
  }
}
