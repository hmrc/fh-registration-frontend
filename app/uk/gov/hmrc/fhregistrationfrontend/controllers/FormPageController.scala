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

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.fhregistrationfrontend.actions.{PageAction, PageRequest, UserAction}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{Page, Rendering}
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.views.html.confirm_delete_section

import scala.concurrent.Future

@Singleton
class FormPageController @Inject()(
  ds               : CommonPlayDependencies,
  messagesApi      : play.api.i18n.MessagesApi,
  links            : ExternalUrls
)(implicit save4LaterService: Save4LaterService) extends AppController(ds, messagesApi) with SubmitForLater {


  def load(pageId: String) = PageAction(pageId).async { implicit request ⇒
    renderForm(request.page, false)
  }

  def loadWithSection(pageId: String, sectionId: String) = PageAction(pageId, Some(sectionId)).async { implicit request ⇒
    renderForm(request.page, false)
  }

  def save[T](pageId: String): Action[AnyContent] = save(pageId, None)
  def saveWithSection[T,V](pageId: String, sectionId: String): Action[AnyContent] = save(pageId, Some(sectionId))

  def save[T](pageId: String, sectionId: Option[String]): Action[AnyContent] = PageAction(pageId, sectionId).async { implicit request ⇒
    request.page[T].parseFromRequest (
      pageWithErrors => renderForm(pageWithErrors, true),
      page => {
        save4LaterService
          .saveData4Later(request.userId, request.page.id, page.data.get)(hc, request.page.format)
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

  def deleteSection[T](pageId: String, sectionId: String, hash: String): Action[AnyContent] = PageAction(pageId, Some(sectionId)).async { implicit request ⇒
    if (request.page[T].data.exists(_.hashCode() == hash)) {
      request.page[T].delete match {
        case None ⇒ Future successful BadRequest("bad request")
        case Some(newPage) ⇒
          save4LaterService
            .saveData4Later(request.userId, request.page.id, newPage.data.get)(hc, request.page.format)
            .map { _ ⇒
              showNextPage(newPage)
            }
      }
    } else {
      Future successful NotFound("Not Found")
    }
  }

  def confirmDeleteSection[T](pageId: String, sectionId: String, hash: String): Action[AnyContent] = PageAction(pageId, Some(sectionId)).async { implicit request ⇒
    println(s"======= $hash, ${request.page[T].data.map(_.hashCode())}")
    if (request.page[T].data.exists(_.hashCode() == hash))
      Future successful Ok(confirm_delete_section(pageId, sectionId, hash))
    else
      Future successful NotFound("Not Found")
  }


  def showNextPage[T](newPage: Page[T])(implicit request: PageRequest[_]) = {
    if (newPage.nextSubsection.isDefined)
      Redirect(routes.FormPageController.loadWithSection(newPage.id, newPage.nextSubsection.get))
    else
      request.journey next newPage.id match {
        case Some(nextPage) ⇒ Redirect(routes.FormPageController.load(nextPage.id))
        case None           ⇒ Redirect(routes.SummaryController.summary())
      }
  }

  private def renderForm[T](page: Rendering, hasErrors: Boolean)(implicit request: PageRequest[_]) = {
    save4LaterService.fetchBusinessRegistrationDetails(request.userId) map {
      case Some(bpr) ⇒
        if (hasErrors)
          BadRequest(page.render(bpr, request.journey.navigation(request.page.id)))
        else {
          Ok(page.render(bpr, request.journey.navigation(request.page.id)))
        }
      case None      ⇒
        Redirect(links.businessCustomerVerificationUrl)
    }
  }

}
