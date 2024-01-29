package forms.$packageName$

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import models.$packageName$.$className$

class $className$FormProvider @Inject() extends Mappings {

  def apply(): Form[$className$] =
    Form(
      "value" -> enumerable[$className$]("$packageName$.$className;format="decap"$.error.required")
    )
}
