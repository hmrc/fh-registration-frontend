package uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import uk.gov.hmrc.fhregistrationfrontend.pages.behaviours.PageBehaviours

class $className$PageSpec extends PageBehaviours {

  "$className$Page" when {

    "$option1key$ selected" should {

      val pageData = $className$.$option1Key$

      beRetrievable[$className$]($className$Page(1), pageData)

      beSettable[$className$]($className$Page(1), pageData)

      beRemovable[$className$]($className$Page(1), pageData)
    }

    "$option2key$ selected" should {

      val pageData = $className$.$option2Key$

      beRetrievable[$className$]($className$Page(1), pageData)

      beSettable[$className$]($className$Page(1), pageData)

      beRemovable[$className$]($className$Page(1), pageData)
    }
  }
}
