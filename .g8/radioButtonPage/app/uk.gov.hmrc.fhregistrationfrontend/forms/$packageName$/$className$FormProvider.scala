package uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$

import javax.inject.Inject

import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings
import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$

class $className$FormProvider @Inject() extends Mappings {

  val key = $className$;format="decap"$
  val requiredErrorKey = "fh.$packageName$.$className;format="decap"$.error.required"
  def apply(): Form[$className$] =
    Form(
      key -> enum(CompanyOfficerType, requiredErrorKey)
    )($className$.apply)($className$.unapply)
}
