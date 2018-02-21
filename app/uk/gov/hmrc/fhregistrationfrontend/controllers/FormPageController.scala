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

import play.api.data.Form
import play.api.mvc.Request
import uk.gov.hmrc.fhregistrationfrontend.actions.{PageAction, PageRequest}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Page
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService

import scala.concurrent.Future

@Singleton
class FormPageController @Inject()(
  ds               : CommonPlayDependencies,
  messagesApi      : play.api.i18n.MessagesApi,
  links            : ExternalUrls
)(implicit save4LaterService: Save4LaterService) extends AppController(ds, messagesApi) with SubmitForLater {


  def load[T](pageId: String) = PageAction(pageId).async { implicit request ⇒
    loadStoredFormData[T](request.userId, request.page) flatMap (renderForm(request.page, _))
  }

  def save[T](pageId: String) = PageAction(pageId).async { implicit request ⇒
    println(s"\n\n${request.page[T].form.bindFromRequest()}\n\n")
    request.page[T].form.bindFromRequest() fold (
      formWithErrors => renderForm(request.page, formWithErrors),
      mainBusinessAddress => {
        save4LaterService
          .saveData4Later(request.userId, request.page.id, mainBusinessAddress)(hc, request.page.format)
          .flatMap { _ ⇒
            if (isSaveForLate)
              Future successful Redirect(routes.Application.savedForLater)
            else {
              request.journey next pageId match {
                case Some(nextPage) ⇒ Future successful Redirect(routes.FormPageController.load(nextPage.id))
                case None           ⇒ Future successful Redirect(routes.Application.summary())
              }
            }
          }
      }
    )
  }

  private def loadStoredFormData[T](uid: String, page: Page[T])(implicit r: Request[_]) =
    save4LaterService.fetchData4Later(uid, page.id)(hc, page.format) map {
      _ map page.form.fill getOrElse page.form
    }

  private def renderForm[T](page: Page[T], form: Form[T])(implicit request: PageRequest[_]) = {
    save4LaterService.fetchBusinessRegistrationDetails(request.userId) map {
      case Some(bpr) ⇒
        if (form.hasErrors)
          BadRequest(page.render(form, bpr, request.journey.navigation(page.id)))
        else {
          Ok(page.render(form, bpr, request.journey.navigation(page.id)))
        }
      case None      ⇒
        Redirect(links.businessCustomerVerificationUrl)
    }
  }

}
