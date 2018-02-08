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
import play.api.mvc.{Request, Result}
import uk.gov.hmrc.fhregistrationfrontend.connectors.FhddsConnector
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.MainBusinessAddressForm.mainBusinessAddressForm
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.{LinearJourney, Page}
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.services.{Save4LaterService, Save4LaterServiceImpl}
import uk.gov.hmrc.fhregistrationfrontend.views.html.forms.main_business_address
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

@Singleton
class FormPageController @Inject()(
  ds               : CommonPlayDependencies,
  messagesApi      : play.api.i18n.MessagesApi,
  links            : ExternalUrls,
  save4LaterService: Save4LaterService
) extends AppController(ds, messagesApi) {

  val journey = new LinearJourney

  def load[T](pageId: String) = authorisedUser { implicit request ⇒
    implicit internalId ⇒
      (journey get[T] pageId) match {
        case Some(page: Page[T]) ⇒
          save4LaterService.fetchData4Later[T](internalId, page.id)(hc, page.format) flatMap {
            case Some(data) ⇒
              println(s"found data $data")
              renderForm(page, page.form.fill(data))
            case None       ⇒
              println(s"no data found")
              renderForm(page, page.form)
          }
        case None                ⇒ Future successful NotFound("Not found")
      }
  }

  def fetchData[T](bpr: BusinessRegistrationDetails, page: Page[T])(implicit request: Request[_], internalId: String): Future[Result] = {
    save4LaterService.fetchData4Later[T](internalId, page.id)(hc, page.format) map {
      case Some(data) ⇒ Ok(page.render(page.form.fill(data), bpr))
      case None       ⇒ Ok(page.render(page.form, bpr))
    }
  }

  private def renderForm[T](page: Page[T], form: Form[T])(implicit request: Request[_], internalId: String) = {
    save4LaterService.fetchBusinessRegistrationDetails(internalId) map {
      case Some(bpr) ⇒
        if (form.hasErrors)
          BadRequest(page.render(form, bpr))
        else {
          println(s"====== form data is $form")
          Ok(page.render(form, bpr))
        }
      case None      ⇒
        Redirect(links.businessCustomerVerificationUrl)
    }
  }

  def save[T](pageId: String) = authorisedUser { implicit request ⇒
    implicit internalId ⇒
      (journey get[T] pageId) match {
        case Some(page: Page[T]) ⇒
          println(s"\n\n ${page.form.bindFromRequest()}\n\n")
          page.form.bindFromRequest() fold(
            formWithErrors => renderForm[T](page, formWithErrors),
            mainBusinessAddress => {
              save4LaterService.saveData4Later[T](internalId, page.id, mainBusinessAddress)(hc, page.format)
              journey next pageId match {
                case Some(nextPage) ⇒ Future successful Redirect(routes.FormPageController.load(nextPage.id))
                case None           ⇒ Future successful Redirect(routes.Application.summary())
              }

            }
          )
        case None                ⇒ Future successful NotFound("Not found")
      }
  }
}
