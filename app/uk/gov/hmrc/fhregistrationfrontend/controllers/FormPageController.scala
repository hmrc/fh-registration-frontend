/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.Logger
import play.api.i18n.Messages
import play.api.mvc.{Action, AnyContent, Request, Results}
import uk.gov.hmrc.fhregistrationfrontend.actions.{Actions, PageAction, PageRequest}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{Page, Rendering}
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.views.html.confirm_delete_section

import scala.concurrent.Future

@Singleton
class FormPageController @Inject()(
  ds               : CommonPlayDependencies,
  links            : ExternalUrls,
  actions: Actions
)(implicit save4LaterService: Save4LaterService) extends AppController(ds) with SubmitForLater {

  import actions._
  def load(pageId: String) = pageAction(pageId).async { implicit request ⇒
    renderForm(request.page, false)
  }

  def loadWithSection(pageId: String, sectionId: String) = pageAction(pageId, Some(sectionId)).async { implicit request ⇒
    renderForm(request.page, false)
  }

  def save[T](pageId: String): Action[AnyContent] = save(pageId, None)
  def saveWithSection[T,V](pageId: String, sectionId: String): Action[AnyContent] = save(pageId, Some(sectionId))

  def save[T](pageId: String, sectionId: Option[String]): Action[AnyContent] = pageAction(pageId, sectionId).async { implicit request ⇒
    request.page[T].parseFromRequest (
      pageWithErrors => renderForm(pageWithErrors, true),
      page => {
        save4LaterService
          .saveDraftData4Later(request.userId, request.page.id, page.data.get)(hc, request.page.format)
          .map { _ ⇒
            if (isSaveForLate)
              Redirect(routes.Application.savedForLater)
            else {
              if (page.nextSubsection.isDefined)
                Redirect(routes.FormPageController.loadWithSection(page.id, page.nextSubsection.get))
              else
                showNextPage(page)
            }
          }
      }
    )
  }

  def deleteSection[T](pageId: String, sectionId: String, lastUpdateTimestamp: Long): Action[AnyContent] =
    pageAction(pageId, Some(sectionId)).async { implicit request ⇒
      if (request.lastUpdateTimestamp == lastUpdateTimestamp) {
        request.page[T].delete match {
          case None          ⇒ Future successful errorHandler.errorResultsPages(Results.BadRequest)
          case Some(newPage) ⇒
            save4LaterService
              .saveDraftData4Later(request.userId, request.page.id, newPage.data.get)(hc, request.page.format)
              .map { _ ⇒
                showNextPage(newPage)
              }
        }
      } else {
        Logger.error(s"Not Found. Expired")
        Future successful errorHandler.errorResultsPages(Results.NotFound)
      }
    }

  def confirmDeleteSection[T](pageId: String, sectionId: String, lastUpdateTimestamp: Long): Action[AnyContent] =
    pageAction(pageId, Some(sectionId)).async { implicit request ⇒
      if (request.lastUpdateTimestamp == lastUpdateTimestamp)
        Future successful Ok(confirm_delete_section(pageId, sectionId, lastUpdateTimestamp))
      else {
        Logger.error(s"Not Found. Expired")
        Future successful errorHandler.errorResultsPages(Results.NotFound)
      }
    }


  def showNextPage[T](newPage: Page[T])(implicit request: PageRequest[_]) = {
    if (newPage.nextSubsection.isDefined)
      Redirect(routes.FormPageController.loadWithSection(newPage.id, newPage.nextSubsection.get))
    else
      request.journey next newPage match {
        case Some(nextPage) ⇒ Redirect(routes.FormPageController.load(nextPage.id))
        case None           ⇒ Redirect(routes.SummaryController.summary())
      }
  }

  private def renderForm[T](page: Rendering, hasErrors: Boolean)(implicit request: PageRequest[_]) = {
    save4LaterService.fetchBusinessRegistrationDetails(request.userId) map {
      case Some(bpr) ⇒
        if (hasErrors)
          BadRequest(page.render(bpr, request.journey.navigation(request.lastUpdateTimestamp, request.page)))
        else {
          Ok(page.render(bpr, request.journey.navigation(request.lastUpdateTimestamp, request.page)))
        }
      case None      ⇒
        errorHandler.errorResultsPages(Results.BadRequest)
    }
  }

}