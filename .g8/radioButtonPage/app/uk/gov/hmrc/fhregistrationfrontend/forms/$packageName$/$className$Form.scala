package uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$

import javax.inject.Inject

import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.`enum`
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$

object $className$Form {

  val requiredErrorKey = "fh.$packageName$.$className;format="decap"$.error.required"
  def apply(): Form[$className$] =
    Form(
      "value" -> enumerable[$className$]("$packageName$.$className;format="decap"$.error.required")
  )
 def apply() = Form(
   "value" -> enum($className$, requiredErrorKey)
  )
}


  def apply(): Form[$className$] =
    Form(
      "value" -> enumerable[$className$]("$packageName$.$className;format="decap"$.error.required")
  )
}