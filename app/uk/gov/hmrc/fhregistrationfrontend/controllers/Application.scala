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

import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.fhregistrationfrontend.connectors._
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessTypeForm.businessTypeForm
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.views.html.registrationstatus._
import uk.gov.hmrc.fhregistrationfrontend.views.html._
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class Application @Inject()(
  links            : ExternalUrls,
  ds               : CommonPlayDependencies,
  fhddsConnector   : FhddsConnector,
  businessCustomerConnector: BusinessCustomerFrontendConnector,
  actions: Actions
)(implicit save4LaterService: Save4LaterService) extends AppController(ds) {


  override val configuration: Configuration = ds.conf

  val formMaxExpiryDays: Int = configuration.getInt(s"formMaxExpiryDays").getOrElse(27)

  import actions._
  def start = userAction { implicit request ⇒
    request.registrationNumber match {
      case Some(_) ⇒ Redirect(routes.Application.checkStatus())
      case None ⇒ Redirect(links.businessCustomerVerificationUrl)
    }
  }

  def continue = userAction.async { implicit request ⇒
    for {
      details ← businessCustomerConnector.getReviewDetails
      _ ← save4LaterService.saveBusinessRegistrationDetails(request.userId, details)
      businessType ← save4LaterService.fetchBusinessType(request.userId)
      hasBusinessType = businessType.isEmpty
    } yield {
      Redirect(routes.Application.deleteOrContinue(hasBusinessType))
    }
  }

  val deleteOrContinueForm = Form("deleteOrContinue" → nonEmptyText)

  def deleteOrContinue(isNewForm: Boolean) = userAction.async { implicit request ⇒
    if (isNewForm) {
      Future successful Redirect(routes.Application.businessType())
    }
    else {
      save4LaterService.fetchLastUpdateTime(request.userId) map {
        case Some(savedDate) ⇒
          Ok(continue_delete(new DateTime(savedDate), deleteOrContinueForm))
        case None            ⇒ Redirect(routes.Application.businessType())
      }
    }
  }

  def submitDeleteOrContinue = userAction.async { implicit request ⇒
    deleteOrContinueForm.bindFromRequest().fold(
      formWithErrors => Future successful errorHandler.errorResultsPages(Results.ServiceUnavailable),
      deleteOrContinue => {
        if (deleteOrContinue == "delete") {
          save4LaterService.fetchLastUpdateTime(request.userId) flatMap {
            case Some(savedDate) ⇒ Future successful Ok(confirm_delete(new DateTime(savedDate)))
            case None            ⇒
              Future successful errorHandler.errorResultsPages(Results.ServiceUnavailable)
          }
        } else {
          Future successful Redirect(routes.Application.resumeForm())
        }
      }
    )
  }

  def confirmDelete = userAction.async { implicit request ⇒
      save4LaterService.fetchLastUpdateTime(request.userId) flatMap {
        case Some(savedDate) ⇒ Future successful Ok(confirm_delete(new DateTime(savedDate)))
        case None            ⇒ Future successful ServiceUnavailable
      }
  }

  def resumeForm = journeyAction { implicit request ⇒
    if(request.journeyState.isComplete)
      Redirect(routes.SummaryController.summary())
    else {
      request.journeyState.lastEditedPage.map( p ⇒ p.id → p.lastSection) match {
        case None ⇒ Redirect(routes.Application.startForm())
        case Some((pid , Some(section))) ⇒  Redirect(routes.FormPageController.loadWithSection(pid, section))
        case Some((pid, None)) ⇒ Redirect(routes.FormPageController.load(pid))

      }
    }
  }

  def deleteUserData = userAction.async { implicit request ⇒
    save4LaterService.removeUserData(request.userId) map {
      _ ⇒ Redirect(routes.Application.start())
    }
  }

  def businessType = userAction { implicit request ⇒
    Ok(business_type(businessTypeForm))
  }

  def submitBusinessType = userAction.async { implicit request ⇒
    businessTypeForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(business_type(formWithErrors))),
      businessType => {
        for {
          _ ← save4LaterService.saveBusinessType(request.userId, businessType)
        } yield {
          Redirect(routes.Application.startForm())
        }
      }
    )
  }

  def startForm = userAction.async { implicit request ⇒
    save4LaterService.fetchBusinessRegistrationDetails(request.userId) map {
      case Some(bpr) ⇒ Redirect(routes.FormPageController.load("mainBusinessAddress"))
      case None      ⇒ Redirect(links.businessCustomerVerificationUrl)
    }
  }

  def savedForLater = userAction.async { implicit request ⇒
    save4LaterService.fetchLastUpdateTime(request.userId).map {
      case Some(savedDate) ⇒ Ok(saved(new DateTime(savedDate).plusDays(formMaxExpiryDays)))
      case None            ⇒ errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def checkStatus() = enrolledUserAction.async { implicit request ⇒
    fhddsConnector
      .getStatus(request.registrationNumber)(hc)
      .map(statusResp ⇒ {
        Ok(status(statusResp.body, request.registrationNumber))
      })
  }

}

@Singleton
abstract class AppController(val ds: CommonPlayDependencies)
  extends FrontendController
    with I18nSupport
    with AuthorisedFunctions {

  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global

  override implicit val messagesApi: MessagesApi = ds.messagesApi

  override implicit def authConnector: AuthConnector = ds.authConnector
  implicit val appConfig: AppConfig = ds.appConfig
  implicit val errorHandler: ErrorHandler = ds.errorHandler

  val configuration: Configuration = ds.conf
  val messages: MessagesApi = messagesApi

  lazy val authProvider: AuthProviders = AuthProviders(GovernmentGateway)
  val hasCtUtr: Predicate = Enrolment("IR-CT")


}

@Singleton
final class CommonPlayDependencies @Inject()(
  val conf: Configuration,
  val appConfig: AppConfig,
  val env: Environment,
  val messagesApi: MessagesApi,
  val errorHandler: ErrorHandler,
  val authConnector: AuthConnector)