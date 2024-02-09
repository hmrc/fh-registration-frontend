package uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.FormSpecsHelper
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec
import scala.util.Random

class $className$FormSpec extends UnitSpec with FormSpecsHelper[$className$.Value] {

  val form: Form[$className$.Value] = $className$Form.form

  "$className$Form" should {
    val validData = Map(
      "value" -> option1key,
      "value" -> option2key
    )

    s"accept valid form for $className$.option1key" in {
      val data = dataFromValidForm(validData)

      data.value shouldBe $className$.option1key
    }

    s"accept valid form for $className$.option2key" in {
      val data = dataFromValidForm(validData)

      data.value shouldBe $className$.option2key
    }

    "reject the form" when {
      val errorPath = "fh.$packageName$.$className;format="decap"$.error."
      def requiredKey = errorPath + "required"

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
