package uk.gov.hmrc.fhregistrationfrontend.models.$packageName$

import models.{Enumerable, WithName}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import play.api.libs.json.{JsError, JsString, Json}

class $className$Spec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "$className$Page" when {

    val pageData = $className$.values.map(_.toString)

    beRetrievable[$className$]($className$Page(1), pageData)

    beSettable[$className$]($className$Page(1), pageData)

    beRemovable[$className$]($className$Page(1), pageData)
  }
}