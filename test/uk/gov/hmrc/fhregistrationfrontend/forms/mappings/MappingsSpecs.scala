package uk.gov.hmrc.fhregistrationfrontend.forms.mappings

import uk.gov.hmrc.play.test.UnitSpec
import play.api.data.Forms._
import org.scalatest.EitherValues

class MappingsSpecs extends UnitSpec with EitherValues {

  "OptionalWithYesAndNo" should {
    "work " in {
      val test = Mappings.optionalFromYesAndNo(number).withPrefix("prefix")

      test.bind(Map("prefix.value" → "123", "prefix.yesNo" → "true")) shouldEqual Right(Some(123))
      test.bind(Map("prefix.yesNo" → "false")) shouldEqual Right(None)



      test.bind(Map("prefix.value" → "123", "prefix.yesNo" → "false")).left.get should have length 1
      test.bind(Map("prefix.value" → "123")).left.get should have length 1


      test.unbind(None) shouldEqual Map("prefix.yesNo" → "false")
      test.unbind(Some(123)) shouldEqual Map("prefix.yesNo" → "true", "prefix.value" → "123")

    }
  }

}
