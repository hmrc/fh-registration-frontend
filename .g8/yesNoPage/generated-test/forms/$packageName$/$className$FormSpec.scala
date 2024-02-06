package uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.FormSpecsHelper
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec
import scala.util.Random

class $className$FormSpec extends UnitSpec with FormSpecsHelper[Boolean] {

  val form = $className$Form.form

  "$className$Form" should {
    "accept valid form" when {
      "true" in {
        val validData = Map(
          "value" -> "true"
        )
        val data = dataFromValidForm(validData)

        data shouldBe true
      }

      "false" in {
        val validData = Map(
          "value" -> "false"
        )
        val data = dataFromValidForm(validData)

        data shouldBe false
      }
    }

    "reject the form" when {
      val requiredKey = "fh.$packageName$.$className;format="decap"$.error.required"

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
