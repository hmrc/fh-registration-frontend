package pages.$packageName$

import models.$packageName$.$className$
import pages.behaviours.PageBehaviours

class $className$Spec extends PageBehaviours {

  "$className$Page" - {

    beRetrievable[$className$]($className$Page)

    beSettable[$className$]($className$Page)

    beRemovable[$className$]($className$Page)
  }
}
