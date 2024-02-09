package uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$

import javax.inject.Inject

import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms._
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings._
import play.api.data.validation.Constraints.maxLength

object $className$Form {

   val form: Form[$className$] = Form(
     mapping(
      "$field1Name$" -> string("fh.$packageName$.$className;format="decap"$.error.$field1Name$.required")
        .verifying(maxLength($field1MaxLength$, "fh.$packageName$.$className;format="decap"$.error.$field1Name$.length")),
      "$field2Name$" -> string("fh.$packageName$.$className;format="decap"$.error.$field2Name$.required")
        .verifying(maxLength($field2MaxLength$, "fh.$packageName$.$className;format="decap"$.error.$field2Name$.length"))
    )($className$.apply)($className$.unapply)
   )
 }
