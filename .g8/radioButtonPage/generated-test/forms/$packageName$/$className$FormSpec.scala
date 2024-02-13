package uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$

import play.api.data.Form
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.FormSpecsHelper
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

class $className$FormSpec extends UnitSpec with FormSpecsHelper[$className$.Value] {

  val form = $className$Form.form

  "$className$Form" should {
    "accept valid form" when {
      "$option1key$ selected" in {
        val validData = Map(
          "value" -> "$option1key$"
        )
        val data = dataFromValidForm(validData)

        data shouldBe $className$.$option1Key$
      }

      "$option2key$ selected" in {
        val validData = Map(
          "value" -> "$option2key$"
        )
        val data = dataFromValidForm(validData)

        data shouldBe $className$.$option2Key$
      }
    }

    "reject the form" when {
      val requiredKey = "fh.$packageName$.$className;format="
      decap"$.error.required"

      "value is empty" in {
        val invalidData = Map(
          "value" -> ""
        )
        formDataHasErrors(
          invalidData,
          List("value" -> requiredKey)
        )
      }
    }
  }
}
