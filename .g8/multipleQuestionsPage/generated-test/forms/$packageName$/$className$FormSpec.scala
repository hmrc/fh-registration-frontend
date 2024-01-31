package uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$

import play.api.data.FormError
import scala.util.Random

class $className$FormSpec extends UnitSpec with FormSpecsHelper[$className$] {

  val form = $className$Form.form

  "$className$Form" should {
    val validData = Map(
      "$field1Name$" -> "test1",
      "$field2Name$" -> "test2"
    )

    "accept valid form" in {
      val data = dataFromValidForm(validData)

      data.$field1Name$ shouldBe "test1"
      data.$field1Name$ shouldBe "test2"
    }

    "reject the form" when {
      val errorPath = "$packageName$.$className;format="decap"$.error."
      def requiredKey(fieldName: String) = errorPath + s"$fieldName.required"
      val lengthKey(fieldName: String) = errorPath + s"$fieldName.length"
      "$field1Name$ exceeds the maxLength" in {
        val fieldExceedingMaxLength: String = Random.nextString($field1MaxLength$ + 1)
        val invalidData = Map(
          "$field1Name$" -> fieldExceedingMaxLength,
          "$field2Name$" -> "test2"
        )
        formDataHasErrors(
          invalidData,
          List($field1Name$ -> lengthKey("$field1Name$"))
        )
      }

      "$field1Name$ is empty" in {
        val invalidData = Map(
          "$field1Name$" -> "",
          "$field2Name$" -> "test2"
        )
        formDataHasErrors(
          invalidData,
          List($field1Name$ -> requiredKey("$field1Name$"))
        )
      }

      "$field2Name$ exceeds the maxLength" in {
        val fieldExceedingMaxLength: String = Random.nextString($field2MaxLength$ + 1)
        val invalidData = Map(
          "$field1Name$" -> "test1",
          "$field2Name$" -> fieldExceedingMaxLength
        )

        formDataHasErrors(
          invalidData,
          List($field2Name$ -> lengthKey("$field2Name$"))
        )
      }

      "$field2Name$ is empty" in {
        val invalidData = Map(
          "$field1Name$" -> "test1",
          "$field2Name$" -> ""
        )
        formDataHasErrors(
          invalidData,
          List($field2Name$ -> requiredKey("$field2Name$"))
        )
      }

      "both fields exceeds the maxLength" in {
        val field1ExceedingMaxLength: String = Random.nextString($field1MaxLength$ + 1)
        val field2ExceedingMaxLength: String = Random.nextString($field2MaxLength$ + 1)
        val invalidData = Map(
          "$field1Name$" -> field1ExceedingMaxLength,
          "$field2Name$" -> field2ExceedingMaxLength
        )
        formDataHasErrors(
          invalidData,
          List($field1Name$ -> lengthKey("$field1Name$"),
            $field2Name$ -> lengthKey("$field2Name$"))
        )
      }

      "both fields are empty" in {
        val invalidData = Map(
          "$field1Name$" -> "",
          "$field2Name$" -> ""
        )

        formDataHasErrors(
          invalidData,
          List($field1Name$ -> requiredKey("$field1Name$"),
            $field2Name$ -> requiredKey("$field2Name$"))
        )
      }
    }
  }
}
