package uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import uk.gov.hmrc.fhregistrationfrontend.pages.behaviours.PageBehaviours

class $className$PageSpec extends PageBehaviours {

  "$className$Page" when {

    val pageData = $className$.Value

    beRetrievable($className$Page(1), pageData)

    beSettable($className$Page(1), pageData)

    beRemovable($className$Page(1), pageData)
  }
}
