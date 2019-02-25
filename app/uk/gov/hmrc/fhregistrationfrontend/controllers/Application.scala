/*
 * Copyright 2019 HM Revenue & Customs
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

import cats.data.OptionT
import cats.implicits._
import org.joda.time.DateTime
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.{Configuration, Environment}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.fhregistrationfrontend.actions.{Actions, UserRequest}
import uk.gov.hmrc.fhregistrationfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.fhregistrationfrontend.connectors._
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessTypeForm.businessTypeForm
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.views.html._
import uk.gov.hmrc.fhregistrationfrontend.views.html.registrationstatus._
import uk.gov.hmrc.fhregistrationfrontend.views.registrationstatus.StatusPageParams
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
  def main = userAction.async { implicit request ⇒

    fhddsConnector
      .getEnrolmentProgress
      .map {
        case EnrolmentProgress.Pending ⇒ Redirect(routes.Application.enrolmentPending)
        case EnrolmentProgress.Error ⇒ Redirect(routes.Application.enrolmentPending)
        case _                         ⇒
          val whenRegistered = request
            .registrationNumber
            .map { _ ⇒ Redirect(routes.Application.checkStatus())}

          whenRegistered getOrElse Redirect(routes.Application.startOrContinueApplication())
      }
  }

  def enrolmentPending = userAction.async { implicit  request ⇒
    fhddsConnector
      .getEnrolmentProgress
      .map {
        case EnrolmentProgress.Pending ⇒ Ok(enrolment_pending())
        case EnrolmentProgress.Error   ⇒ Ok(application_error())
        case EnrolmentProgress.Unknown ⇒ Redirect(routes.Application.main())
      }
  }

  private val deleteOrContinueForm = Form("deleteOrContinue" → nonEmptyText)

  def startOrContinueApplication = userAction.async { implicit request ⇒
    val redirectWhenSaved = for {
      _ ← OptionT(save4LaterService.fetchBusinessRegistrationDetails(request.userId))
      _ ← OptionT(save4LaterService.fetchBusinessType(request.userId))
      savedDate ← OptionT(save4LaterService.fetchLastUpdateTime(request.userId))
    } yield {
      Ok(continue_delete(new DateTime(savedDate), deleteOrContinueForm))
    }

    redirectWhenSaved getOrElseF newApplication
  }

  private def newApplication(implicit request: UserRequest[_]) = {
    save4LaterService
      .fetch(request.userId).map {
        case Some(_) ⇒ save4LaterService.removeUserData(request.userId)
        case None ⇒
      }
      .map(_ ⇒ Redirect(links.businessCustomerVerificationUrl))
  }


  def continueWithBpr = newApplicationAction.async { implicit request ⇒
    for {
      details ← businessCustomerConnector.getReviewDetails
      _ ← save4LaterService.saveBusinessRegistrationDetails(request.userId, details)
    } yield {
      Redirect(routes.Application.businessType())
    }
  }


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
      formWithErrors => Future successful errorHandler.errorResultsPages(Results.BadRequest),
      deleteOrContinue => {
        if (deleteOrContinue == "delete") {
          save4LaterService.fetchLastUpdateTime(request.userId) flatMap {
            case Some(savedDate) ⇒ Future successful Ok(confirm_delete(new DateTime(savedDate)))
            case None            ⇒
              Future successful errorHandler.errorResultsPages(Results.BadRequest)
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
        case None            ⇒ Future successful BadRequest
      }
  }

  def resumeForm = journeyAction { implicit request ⇒
    if(request.journeyState.isComplete)
      Redirect(routes.SummaryController.summary())
    else {
      request.journeyState.lastEditedPage.map( p ⇒ p.id → p.lastSection) match {
        case None ⇒
          val firstPage = request.journeyPages.pages.head.id
          Redirect(routes.FormPageController.load(firstPage))
        case Some((pid , Some(section))) ⇒  Redirect(routes.FormPageController.loadWithSection(pid, section))
        case Some((pid, None)) ⇒ Redirect(routes.FormPageController.load(pid))

      }
    }
  }

  def deleteUserData = userAction.async { implicit request ⇒
    save4LaterService.removeUserData(request.userId) map {
      _ ⇒ Redirect(routes.Application.main())
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
          Redirect(routes.EmailVerificationController.contactEmail())
        }
      }
    )
  }

  def startForm = userAction { implicit request ⇒
    Redirect(routes.Application.resumeForm())
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
      .map(fhddsStatus ⇒ {
        Ok(status(StatusPageParams(fhddsStatus).get, request.registrationNumber))
      })
  }

}

@Singleton
abstract class AppController(val ds: CommonPlayDependencies)
  extends FrontendController
    with I18nSupport {

  override implicit val messagesApi: MessagesApi = ds.messagesApi

  implicit val appConfig: AppConfig = ds.appConfig
  implicit val errorHandler: ErrorHandler = ds.errorHandler

  val configuration: Configuration = ds.conf
  val messages: MessagesApi = messagesApi


}

@Singleton
class CommonPlayDependencies @Inject()(
  val conf: Configuration,
  val appConfig: AppConfig,
  val env: Environment,
  val messagesApi: MessagesApi,
  val errorHandler: ErrorHandler)