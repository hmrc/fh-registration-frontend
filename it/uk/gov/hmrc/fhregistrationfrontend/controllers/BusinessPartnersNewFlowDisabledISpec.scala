package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, NormalMode}
import play.api.libs.ws.DefaultWSCookie
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersNewFlowDisabledISpec
  extends Specifications with TestConfiguration {

  override val newBusinessPartnersFlowEnabled: Boolean = false

  val index = 1
  val normalRoutes = List(
    routes.BusinessPartnersController.load(index, NormalMode),
    routes.BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController.load(index, NormalMode),
    routes.BusinessPartnersConfirmAddressController.load(index, NormalMode)
  )
  val checkRoutes = List(
    routes.BusinessPartnersController.load(index, CheckMode),
    routes.BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController.load(index, CheckMode),
    routes.BusinessPartnersConfirmAddressController.load(index, CheckMode)
  )

  (normalRoutes ++ checkRoutes).foreach { route =>
    s"GET $route" when {
      "the new business partners flow is disabled" should {

        "render the not found error page" in {
          given
            .commonPrecondition

          val result = buildRequestFromRoute(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie)).get()

          whenReady(result) { res =>
            res.status mustBe 404
          }
        }
      }
    }

    s"POST $route" when {

      "the new business partners flow is disabled" should {

        "render the not found error page" in {
          given
            .commonPrecondition

          val result = buildRequestFromRoute(route)
            .addCookies(
              DefaultWSCookie("mdtp", authAndSessionCookie),
              DefaultWSCookie("businessType", "individual")
            )
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post("")

          whenReady(result) { res =>
            res.status mustBe 404
          }
        }
      }
    }
  }
}