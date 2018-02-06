package uk.gov.hmrc.fhregistrationfrontend.models.formmodel

import uk.gov.hmrc.play.test.UnitSpec

class MainBusinessAddressSpecs extends UnitSpec {
  val form = MainBusinessAddress.mainBusinessAddressForm

  "MainBusinessAddress form binding" should {

    "validate a form over three years" in {
      val data = Map(
        "timeAtCurrentAddress" -> "5 to 10 years"
      )
      val result = form.bind(data).value.get
      result shouldBe MainBusinessAddress("5 to 10 years", None, None)
    }

    "validate a form lese than three years but without previous address" in {
      val data = Map(
        "timeAtCurrentAddress" -> "Less than 3 years",
        "previousAddress_yesNo" → "false"
      )
      val result = form.bind(data).value.get
      result shouldBe MainBusinessAddress("Less than 3 years", Some(false), None)
    }

    "validate a form less than three years and with previous address" in {
      val data = Map(
        "timeAtCurrentAddress" -> "Less than 3 years",
        "previousAddress_yesNo" → "true",
        "previousAddress.addressLine1" → "line1",
        "previousAddress.addressLine2" → "line2",
        "previousAddress.postcode" → "postcode",
        "previousAddress.countryCode" → "countryCode"
      )
      val result = form.bind(data).value.get
      result shouldBe MainBusinessAddress(
        "Less than 3 years",
        Some(true),
        Some(AddressModel("line1","line2",None,None, "postcode", Some("countryCode"))))
    }
  }

  "MainBusinessAddress form unbinding" should {
    "unbind a form wtih address" in {
      val data = form.fill(MainBusinessAddress("Less than 3 years", Some(true), Some(AddressModel("line1","line2",None,None, "postcode", Some("countryCode")))))

      data shouldBe Map(
        "timeAtCurrentAddress" -> "Less than 3 years",
        "previousAddress_yesNo" → "true",
        "previousAddress.addressLine1" → "line1",
        "previousAddress.addressLine2" → "line2",
        "previousAddress.postcode" → "postcode",
        "previousAddress.countryCode" → "countryCode"
      )
    }

  }
}
