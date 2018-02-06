package uk.gov.hmrc.fhregistrationfrontend.forms.definitions

import uk.gov.hmrc.fhregistrationfrontend.forms.models.VatNumber
import uk.gov.hmrc.play.test.UnitSpec

class VatNumberFormSpecs extends UnitSpec {

  val form = VatNumberForm.vatNumberForm

  "valid VatNumberForm" should {

    "Bind w/o vat number" in {
      val data = Map(
        "yesNo" → "false"
      )

      form.bind(data).get shouldBe VatNumber(None)
    }

    "Bind with vat number" in {
      val data = Map(
        "yesNo" → "true",
        "value" → "123456789"
      )

      form.bind(data).get shouldBe VatNumber(Some("123456789"))
    }
  }

}
