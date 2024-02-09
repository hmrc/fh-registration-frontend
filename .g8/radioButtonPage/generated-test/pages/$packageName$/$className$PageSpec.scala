package uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import uk.gov.hmrc.fhregistrationfrontend.pages.behaviours.PageBehaviours

class $className$PageSpec extends PageBehaviours {

  "$className$Page" when {

    val pageDataOption1 = $className$.values.head
    val pageDataOption2 = $className$.values.last

    beRetrievable($className$Page(1), pageDataOption1)

    beSettable($className$Page(1), pageDataOption1)

    beRemovable($className$Page(1), pageDataOption1)

    beRetrievable($className$Page(1), pageDataOption2)

    beSettable($className$Page(1), pageDataOption2)

    beRemovable($className$Page(1), pageDataOption2)
  }
}
