///*
// * Copyright 2023 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.fhregistrationfrontend.forms.definitions
//
//import uk.gov.hmrc.fhregistrationfrontend.forms.TestData
//import uk.gov.hmrc.fhregistrationfrontend.forms.models.MainBusinessAddress
//import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec
//
//class MainBusinessAddressFormSpecs extends UnitSpec with FormSpecsHelper[MainBusinessAddress] {
//
//  import MainBusinessAddressForm._
//
//  val form = MainBusinessAddressForm.mainBusinessAddressForm
//
//  val valid = Map(
//    timeAtCurrentAddressKey -> "3 to 5 years"
//  )
//
//  val validWithNoPreviousAddress = Map(
//    timeAtCurrentAddressKey -> "Less than 3 years",
//    previousAddressKey      -> "false"
//  )
//
//  val validWithPreviousAddress = Map(
//    timeAtCurrentAddressKey               -> "Less than 3 years",
//    previousAddressKey                    -> "true",
//    s"$previousAddressStartdateKey.day"   -> "31",
//    s"$previousAddressStartdateKey.month" -> "7",
//    s"$previousAddressStartdateKey.year"  -> "2015"
//  ) ++ TestData.addressDataUk(mainPreviousAddressKey)
//
//  "Business address form " should {
//
//    "reject missing mandatory" in {
//      formDataHasErrors(
//        Map.empty,
//        List(timeAtCurrentAddressKey -> "error.required")
//      )
//    }
//
//    "reject Less than 3 years w/o prevAddress answer" in {
//      formDataHasErrors(
//        Map(timeAtCurrentAddressKey -> "Less than 3 years"),
//        List(previousAddressKey     -> "error.required")
//      )
//    }
//
//    "reject has previous address with no address" in {
//      formDataHasErrors(
//        validWithNoPreviousAddress + (previousAddressKey -> "true"),
//        List(s"$mainPreviousAddressKey.Line1" -> "error.required")
//      )
//    }
//
//    "accept valid form data" in {
//
//      val data = dataFromValidForm(valid)
//      data.timeAtCurrentAddress shouldBe "3 to 5 years"
//      data.hasPreviousAddress shouldBe None
//      data.previousAddress shouldBe None
//    }
//
//    "accept validWithNoPreviousAddress form data" in {
//
//      val data = dataFromValidForm(validWithNoPreviousAddress)
//      data.timeAtCurrentAddress shouldBe "Less than 3 years"
//      data.hasPreviousAddress shouldBe Some(false)
//      data.previousAddress shouldBe None
//    }
//
//    "accept validWithPreviousAddress form data" in {
//
//      val data = dataFromValidForm(validWithPreviousAddress)
//      data.timeAtCurrentAddress shouldBe "Less than 3 years"
//      data.hasPreviousAddress shouldBe Some(true)
//      data.previousAddress shouldBe Some(TestData.addressUk)
//    }
//  }
//
//}
