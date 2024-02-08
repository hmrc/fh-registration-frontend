package uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.pages.behaviours.PageBehaviours

class $className$PageSpec extends PageBehaviours {

  "$className$Page" when {

    val pageData = "test1"

    beRetrievable[String]($className$Page(1), pageData)

    beSettable[String]($className$Page(1), pageData)

    beRemovable[String]($className$Page(1), pageData)
  }
}
