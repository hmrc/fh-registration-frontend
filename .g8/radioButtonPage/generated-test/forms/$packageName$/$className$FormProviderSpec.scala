package uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.FormSpecsHelper
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec
import scala.util.Random

class $className$FormSpec extends UnitSpec with FormSpecsHelper[$className$] {

  val form = $className$Form.form

  "$className$Form" should {
    val validData = Map(
      "value" -> "option1"
    )

    "accept valid form" in {
      val data = dataFromValidForm(validData)

      data.value shouldBe "option1"
    }

    "reject the form" when {
      val errorPath = "fh.$packageName$.$className;format="decap"$.error."
      def requiredKey(fieldName: String) = errorPath + fieldName + ".required"

      "value is empty" in {
        val invalidData = Map(
          "value" -> ""
        )
        formDataHasErrors(
          invalidData,
          List("value" -> requiredKey("value"))
        )
      }
    }
  }
}
