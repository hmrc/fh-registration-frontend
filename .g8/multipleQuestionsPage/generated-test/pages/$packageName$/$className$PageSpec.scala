package uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import uk.gov.hmrc.fhregistrationfrontend.pages.behaviours.PageBehaviours

class $className$PageSpec extends PageBehaviours {

  "$className$Page" - {

    val pageData = $className$("test1", "test2")

    beRetrievable[$className$]($className$Page, pageData)

    beSettable[$className$]($className$Page, pageData)

    beRemovable[$className$]($className$Page, pageData)
  }
}
