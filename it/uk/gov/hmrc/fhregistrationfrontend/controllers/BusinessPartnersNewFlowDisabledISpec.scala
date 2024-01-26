package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, NormalMode}
import play.api.libs.ws.DefaultWSCookie
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnersNewFlowDisabledISpec
  extends Specifications with TestConfiguration {

  override val newBusinessPartnersFlowEnabled: Boolean = false

  val index = 1
  val normalRoutes = List(
    routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.load(index, NormalMode),
    routes.BusinessPartnersPartnershipConfirmRegisteredAddressController.load(index),
    routes.BusinessPartnersPartnershipUtrController.load(index, NormalMode),
    routes.BusinessPartnersController.load(index, NormalMode),
    routes.BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController.load(index, NormalMode),
    routes.BusinessPartnersConfirmAddressController.load(index, NormalMode),
    routes.BusinessPartnersEnterAddressController.load(index, NormalMode),
    routes.BusinessPartnersIndividualsAndSoleProprietorsNinoController.load(index, NormalMode),
    routes.BusinessPartnersChooseAddressController.load(index, NormalMode),
    routes.BusinessPartnersCannotFindAddressController.load(index, NormalMode),
    routes.BusinessPartnersAddressController.load(index, NormalMode),
    routes.BusinessPartnersSoleProprietorsVatRegistrationNumberController.load(index, NormalMode),
    routes.BusinessPartnersPartnershipNameController.load(index, NormalMode),
    routes.BusinessPartnersPartnershipTradingNameController.load(index, NormalMode),
    routes.BusinessPartnersPartnershipVatNumberController.load(index, NormalMode),
    routes.BusinessPartnersPartnershipEnterAddressController.load(index, NormalMode),
    routes.BusinessPartnersSoleProprietorsTradingNameController.load(index, NormalMode),
    routes.BusinessPartnersPartnershipRegisteredAddressController.load(index, NormalMode)
  )

  val checkRoutes = List(
    routes.BusinessPartnersController.load(index, CheckMode),
    routes.BusinessPartnersPartnershipCompanyRegistrationNumberController.load(index, CheckMode),
    routes.BusinessPartnersPartnershipUtrController.load(index, CheckMode),
    routes.BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController.load(index, CheckMode),
    routes.BusinessPartnersConfirmAddressController.load(index, CheckMode),
    routes.BusinessPartnersEnterAddressController.load(index, CheckMode),
    routes.BusinessPartnersIndividualsAndSoleProprietorsNinoController.load(index, CheckMode),
    routes.BusinessPartnersChooseAddressController.load(index, CheckMode),
    routes.BusinessPartnersCannotFindAddressController.load(index, CheckMode),
    routes.BusinessPartnersAddressController.load(index, CheckMode),
    routes.BusinessPartnersSoleProprietorsVatRegistrationNumberController.load(index, CheckMode),
    routes.BusinessPartnersPartnershipNameController.load(index, CheckMode),
    routes.BusinessPartnersPartnershipTradingNameController.load(index, CheckMode),
    routes.BusinessPartnersPartnershipVatNumberController.load(index, CheckMode),
    routes.BusinessPartnersPartnershipEnterAddressController.load(index, CheckMode),
    routes.BusinessPartnersSoleProprietorsTradingNameController.load(index, CheckMode),
    routes.BusinessPartnersPartnershipRegisteredAddressController.load(index, CheckMode)
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