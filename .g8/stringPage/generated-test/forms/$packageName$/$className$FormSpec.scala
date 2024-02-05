package uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.FormSpecsHelper
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec
import scala.util.Random

class $className$FormSpec extends UnitSpec with FormSpecsHelper[String] {

  val form = $className$Form.form

  "$className$Form" should {
    val validData = Map(
      "value" -> "test1"
    )

    "accept valid form" in {
      val data = dataFromValidForm(validData)

      data.value shouldBe "test1"
    }

    "reject the form" when {
      val errorPath = "fh.$packageName$.$className;format="decap"$.error."
      val requiredKey = "fh.$packageName$.$className;format="decap"$.error.required"
      val lengthKey = "fh.$packageName$.$className;format="decap"$.error.length"
      "value exceeds the maxLength" in {
        val fieldExceedingMaxLength: String = Random.nextString($maxLength$ + 1)
        val invalidData = Map(
          "value" -> fieldExceedingMaxLength)
        formDataHasErrors(
          invalidData,
          List("value" -> lengthKey)
        )
      }

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
