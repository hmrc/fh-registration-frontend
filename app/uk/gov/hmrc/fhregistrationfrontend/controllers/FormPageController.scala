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
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions.{Actions, PageRequest}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page.AnyPage
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{Journeys, Page, Rendering}
import uk.gov.hmrc.fhregistrationfrontend.services.{AddressAuditService, Save4LaterService}
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.play.bootstrap.controller.WithUrlEncodedAndMultipartFormBinding

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FormPageController @Inject()(
  ds: CommonPlayDependencies,
  addressAuditService: AddressAuditService,
  cc: MessagesControllerComponents,
  journeys: Journeys,
  actions: Actions,
  views: Views
)(implicit save4LaterService: Save4LaterService, ec: ExecutionContext)
    extends AppController(ds, cc) {

  import actions._
  def load(pageId: String) = pageAction(pageId) { implicit request =>
    renderForm(request.page, false)
  }

  def loadNextPage(pageId: String) = nextPageAction(pageId) { implicit request =>
//    TODO: THIS IS NOT USED AT ALL
    renderForm(request.page, false)
  }

  def loadWithSection(pageId: String, sectionId: String) =
    pageAction(pageId, Some(sectionId)) { implicit request =>
      renderForm(request.page, false)
    }

  def save[T](pageId: String): Action[AnyContent] = save(pageId, None)
  def saveWithSection[T, V](pageId: String, sectionId: String): Action[AnyContent] = save(pageId, Some(sectionId))

  def save[T](pageId: String, sectionId: Option[String]): Action[AnyContent] = pageAction(pageId, sectionId).async {
    implicit request =>
      request
        .page[T]
        .parseFromRequest(
          pageWithErrors => Future successful renderForm(pageWithErrors, true),
          page => {
            addressAuditService.auditAddresses(pageId, page.updatedAddresses)
            save4LaterService
              .saveDraftData4Later(request.userId, request.page.id, page.data.get)(hc, request.page.format)
              .map {
                value =>
                  if (isSaveForLate)
                    Redirect(routes.Application.savedForLater)
                  else {
//                  REALISTICIALLY WOULD PREFER TO FIND CORRECT NEXT PAGE IN THIS FUNCTION
//                  THE PROBLEM IS THE REQUEST WE HAVE HERE HAS THE JOURNEY PAGES/STATE WITHOUT THE SAVED VALUE
//                  WE COULD PASS IN THE PAGE AND SAVED VALUE AND THEN WORK OUT IN THERE LIKE
                    findNextPage(page, value)
//                  showNextPage(page)
                  }
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

//  WE THEN NEED TO BUILD THIS FUNCTION CORRECTLY - ESSENTIALLY REFORMING THE PAGE REQUEST VALUE (THIS SEEMS INEFFICIENT)
  def getFollowingPage[T](newPage: Page[T], value: Option[T])(implicit request: PageRequest[_]): Option[AnyPage] = {
    val initJourneyState = request.journeyState
    val newJourneyState = initJourneyState.overwrite(newPage, value)
    journeys.getNextPageFromJourneysAndCurrentPageIdAlt(request.journey, newJourneyState, newPage)
  }

  private def findNextPage[T](newPage: Page[T], value: Option[T])(implicit request: PageRequest[_]): Result =
    if (newPage.nextSubsection.isDefined)
      Redirect(routes.FormPageController.loadWithSection(newPage.id, newPage.nextSubsection.get))
    else {
      val followingPage = getFollowingPage(newPage, value)
      followingPage match {
        case Some(nextPage) => Redirect(routes.FormPageController.load(nextPage.id))
        case None           => Redirect(routes.SummaryController.summary)
      }
    }

  private def showNextPage[T](newPage: Page[T])(implicit request: PageRequest[_]): Result =
    if (newPage.nextSubsection.isDefined)
      Redirect(routes.FormPageController.loadWithSection(newPage.id, newPage.nextSubsection.get))
    else
      request.journey next newPage match {
        case Some(nextPage) => Redirect(routes.FormPageController.loadNextPage(newPage.id))
        case None           => Redirect(routes.SummaryController.summary)
      }

  private def renderForm[T](page: Rendering, hasErrors: Boolean)(implicit request: PageRequest[_]) =
    if (hasErrors)
      BadRequest(page.render(request.bpr, request.journey.navigation(request.lastUpdateTimestamp, request.page)))
    else {
      Ok(page.render(request.bpr, request.journey.navigation(request.lastUpdateTimestamp, request.page)))
    }

  val submitButtonValueForm: Form[String] = Form("saveAction" -> nonEmptyText)

  /** returns true only when the form contains an 'saveAction' button with value == 'saveForLater'*/
  private def isSaveForLate(implicit req: Request[_]): Boolean =
    submitButtonValueForm
      .bindFromRequest()
      .fold(
        _ => false,
        value => value == "saveForLater"
      )
}
