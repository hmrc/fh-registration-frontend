package uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$

import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.`enum`
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$

object $className$Form {

  val requiredErrorKey = "fh.$packageName$.$className;format="decap"$.error.required"
  val form: Form[$className$.Value] =
    Form(
      "value" -> enum($className$, requiredErrorKey)
  )

}
