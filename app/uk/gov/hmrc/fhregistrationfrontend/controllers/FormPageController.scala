/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.fhregistrationfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.data.{Form, FormError}
import play.api.data.Forms.{nonEmptyText, tuple}
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result, Results}
import play.twirl.api.Html
import uk.gov.hmrc.fhregistrationfrontend.actions.{Actions, PageRequest}
import uk.gov.hmrc.fhregistrationfrontend.config.AppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.{BusinessPartnersForm, VatNumberForm}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey._
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{skippingOnePrefix, yesOrNo}
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{BusinessPartner, ListWithTrackedChanges, VatNumber}
import uk.gov.hmrc.fhregistrationfrontend.forms.navigation.Navigation
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.services.{AddressAuditService, Save4LaterService}
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.views.helpers.RepeatingPageParams

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FormPageController @Inject() (
  ds: CommonPlayDependencies,
  addressAuditService: AddressAuditService,
  cc: MessagesControllerComponents,
  actions: Actions,
  views: Views
)(implicit save4LaterService: Save4LaterService, ec: ExecutionContext)
    extends AppController(ds, cc) {

  import actions._
  def load(pageId: String) = pageAction(pageId) { implicit request =>
    renderForm(request.page, false)
  }

  def loadWithSection(pageId: String, sectionId: String) =
    pageAction(pageId, Some(sectionId)) { implicit request =>
      renderForm(request.page, false)
    }

  def save[T](pageId: String): Action[AnyContent] = save(pageId, None)
  def saveWithSection[T, V](pageId: String, sectionId: String): Action[AnyContent] = save(pageId, Some(sectionId))

  private def saveSuccessfully[T](page: Page[T])(implicit request: PageRequest[AnyContent]): Future[Result] = {
    addressAuditService.auditAddresses(page.id, page.updatedAddresses)
    save4LaterService
      .saveDraftData4Later(request.userId, request.page.id, page.data.get)(hc, request.page.format)
      .map { _ =>
        if (isSaveForLate)
          Redirect(routes.Application.savedForLater)
        else {
          showNextPage(page)
        }
      }
  }

  def save[T](pageId: String, sectionId: Option[String]): Action[AnyContent] =
    pageId match {
      case "vatNumber" => saveVatNumber()
      case "businessPartners" => saveBusinessPartners(sectionId)
      case _ =>
        pageAction(pageId, sectionId).async { implicit request =>
          request
            .page[T]
            .parseFromRequest(
              pageWithErrors => Future successful renderForm(pageWithErrors, true),
              page => saveSuccessfully(page)
            )
        }
    }

  def saveVatNumber(): Action[AnyContent] =
    pageAction("vatNumber", None).async { implicit request =>
      request
        .page[VatNumber]
        .parseFromRequest(
          pageWithErrors => Future successful renderForm(pageWithErrors, true),
          page => {
            val pageData = page.data.get
            val usedVatNumbers: List[String] = request.otherUsedVatNumbers(pageData)
            if (!pageData.value.exists(usedVatNumbers.contains)) {
              saveSuccessfully(page)
            } else {
              val vatNumberBasicPage = new BasicPage[VatNumber](
                "vatNumber",
                VatNumberForm.vatNumberForm,
                new FormRendering[VatNumber] {
                  override def render(
                    form: Form[VatNumber],
                    bpr: BusinessRegistrationDetails,
                    navigation: Navigation
                  )(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
                    views.vat_registration(
                      form,
                      navigation,
                      uk.gov.hmrc.fhregistrationfrontend.controllers.routes.FormPageController
                        .save("vatNumber")
                    )(request, request2Messages(request))
                }
              )
//              TODO: PASS BELOW VALUE INTO basic page and create renderWithFormError method
              val updatedForm: Form[VatNumber] = VatNumberForm.vatNumberForm.copy(
                data = Map(
                  "vatNumber_yesNo" -> pageData.hasValue.toString,
                  "vatNumber_value" -> pageData.value.getOrElse("")
                ),
                errors = Seq(FormError("vatNumber_value", List("error.vatAlreadyUsed"), List()))
              )
              Future successful BadRequest(
                vatNumberBasicPage.renderWithUpdatedForm(
                  updatedForm,
                  request.bpr,
                  request.journey.navigation(request.lastUpdateTimestamp, request.page)
                )(request, request2Messages(request), appConfig)
              )
            }
          }
        )
    }

  def saveBusinessPartners(sectionId: Option[String]): Action[AnyContent] =
    pageAction("businessPartners", sectionId).async { implicit request =>
      request
        .page[(BusinessPartner, Boolean)]
        .parseFromRequest(
          pageWithErrors => Future successful renderForm(pageWithErrors, true),
          page => {
            val pageData = page.data.get
            val usedVatNumbers: List[String] = request.otherUsedVatNumbers(pageData._1, sectionId)
            if (false) {
              saveSuccessfully(page)
            } else {
              val businessPartnersPage = new RepeatingPage[BusinessPartner](
                "businessPartners",
                new RepeatedFormRendering[(BusinessPartner, Boolean)] {
                  override def render(
                                       form: Form[(BusinessPartner, Boolean)],
                                       bpr: BusinessRegistrationDetails,
                                       navigation: Navigation,
                                       sectionId: String,
                                       params: RepeatingPageParams
                                     )(implicit request: Request[_], messages: Messages, appConfig: AppConfig): Html =
                    views.business_partners(form, navigation, sectionId, params)(request, request2Messages(request))
                },
                BusinessPartnersForm.businessPartnerMapping,
                value = request.businessPartners(),
                minItems = 2,
                addressOnPage = { bp =>
                  Some(bp.identification.address)
                }
              )
              Future successful BadRequest(
                businessPartnersPage.renderWithFormError(
//                  TODO: SUSPECT ONLY WORKS FOR SOLE PROPRIETOR LIKE THIS
                  Seq(FormError("businessPartnerSoleProprietor_vat_value", List("error.vatAlreadyUsed"), List())),
                  request.bpr,
                  request.journey.navigation(request.lastUpdateTimestamp, request.page),
                  sectionId.get
                )(request, request2Messages(request), appConfig)
              )
            }
          }
        )
    }

  def deleteSection[T](pageId: String, sectionId: String, lastUpdateTimestamp: Long): Action[AnyContent] =
    pageAction(pageId, Some(sectionId)).async { implicit request =>
      if (request.lastUpdateTimestamp == lastUpdateTimestamp) {
        request.page[T].delete match {
          case None => Future successful errorHandler.errorResultsPages(Results.BadRequest)
          case Some(newPage) =>
            save4LaterService
              .saveDraftData4Later(request.userId, request.page.id, newPage.data.get)(hc, request.page.format)
              .map { _ =>
                showNextPage(newPage)
              }
        }
      } else {
        Future successful errorHandler.errorResultsPages(Results.NotFound)
      }
    }

  def confirmDeleteSection[T](pageId: String, sectionId: String, lastUpdateTimestamp: Long): Action[AnyContent] =
    pageAction(pageId, Some(sectionId)).async { implicit request =>
      if (request.lastUpdateTimestamp == lastUpdateTimestamp)
        Future successful Ok(views.confirm_delete_section(pageId, sectionId, lastUpdateTimestamp))
      else {
        Future successful errorHandler.errorResultsPages(Results.NotFound)
      }
    }

  private def showNextPage[T](newPage: Page[T])(implicit request: PageRequest[_]) =
    if (newPage.nextSubsection.isDefined)
      Redirect(routes.FormPageController.loadWithSection(newPage.id, newPage.nextSubsection.get))
    else
      request.journey next newPage match {
        case Some(nextPage) => Redirect(routes.FormPageController.load(nextPage.id))
        case None           => Redirect(routes.SummaryController.summary)
      }

  private def renderForm[T](page: Rendering, hasErrors: Boolean)(implicit request: PageRequest[_]) =
    if (hasErrors)
      BadRequest(page.render(request.bpr, request.journey.navigation(request.lastUpdateTimestamp, request.page)))
    else {
      Ok(page.render(request.bpr, request.journey.navigation(request.lastUpdateTimestamp, request.page)))
    }

  val submitButtonValueForm: Form[String] = Form("saveAction" -> nonEmptyText)

  /** returns true only when the form contains an 'saveAction' button with value == 'saveForLater' */
  private def isSaveForLate(implicit req: Request[_]): Boolean =
    submitButtonValueForm
      .bindFromRequest()
      .fold(
        _ => false,
        value => value == "saveForLater"
      )
}
