package uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.pages.behaviours.PageBehaviours

class $className$PageSpec extends PageBehaviours {

  "$className$Page" when {

    "true selected" should {

      val pageData = true

      beRetrievable[Boolean]($className$Page(1), pageData)

      beSettable[Boolean]($className$Page(1), pageData)

      beRemovable[Boolean]($className$Page(1), pageData)
    }

    "false selected" should {

      val pageData = false

      beRetrievable[Boolean]($className$Page(1), pageData)

      beSettable[Boolean]($className$Page(1), pageData)

      beRemovable[Boolean]($className$Page(1), pageData)
    }
  }
}
