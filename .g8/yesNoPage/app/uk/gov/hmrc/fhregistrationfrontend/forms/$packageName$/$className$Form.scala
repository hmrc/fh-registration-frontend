package uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$

import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings._
import play.api.data.validation.Constraints.maxLength

object $className$Form {

   val form: Form[Boolean] = Form(
      "value" -> yesOrNo("fh.$packageName$.$className;format="decap"$.error.required")
   )
 }
