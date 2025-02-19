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

import cats.data.OptionT
import cats.implicits.*

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms.nonEmptyText
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.*
import play.api.Logging
import play.api.{Configuration, Environment}
import uk.gov.hmrc.fhregistrationfrontend.actions.{Actions, JourneyRequest, UserRequest}
import uk.gov.hmrc.fhregistrationfrontend.config.{AppConfig, ErrorHandler}
import uk.gov.hmrc.fhregistrationfrontend.connectors.*
import uk.gov.hmrc.fhregistrationfrontend.controllers.ExternalUrls
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessTypeForm.businessTypeForm
import uk.gov.hmrc.fhregistrationfrontend.models.fhregistration.EnrolmentProgress
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.utils.dateTimeHelper
import uk.gov.hmrc.fhregistrationfrontend.views.{ViewHelpers, Views}
import uk.gov.hmrc.fhregistrationfrontend.views.html.registrationstatus.status
import uk.gov.hmrc.fhregistrationfrontend.views.registrationstatus.StatusPageParams
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Application @Inject() (
  links: ExternalUrls,
  ds: CommonPlayDependencies,
  fhddsConnector: FhddsConnector,
  businessCustomerConnector: BusinessCustomerFrontendConnector,
  cc: MessagesControllerComponents,
  actions: Actions,
  views: Views,
  status: status,
  statusParams: StatusPageParams.StatusParams
)(implicit save4LaterService: Save4LaterService, ec: ExecutionContext)
    extends AppController(ds, cc) {

  override val configuration: Configuration = ds.conf

  val formMaxExpiryDays: Int = configuration.getOptional[Int](s"formMaxExpiryDays").getOrElse(27)

  import actions._
  def main = userAction.async { implicit request =>
    fhddsConnector.getEnrolmentProgress
      .map {
        case EnrolmentProgress.Pending => Redirect(routes.Application.enrolmentPending())
        case EnrolmentProgress.Error   => Redirect(routes.Application.enrolmentPending())
        case _ =>
          val whenRegistered = request.registrationNumber
            .map { _ =>
              Redirect(routes.Application.checkStatus())
            }

          whenRegistered getOrElse Redirect(routes.Application.startOrContinueApplication())
      }
  }

  def enrolmentPending = userAction.async { implicit request =>
    fhddsConnector.getEnrolmentProgress
      .map {
        case EnrolmentProgress.Pending => Ok(views.enrolment_pending())
        case EnrolmentProgress.Error   => Ok(views.application_error())
        case _                         => Redirect(routes.Application.main())
      }
  }

  private val deleteOrContinueForm = Form("deleteOrContinue" -> nonEmptyText)

  def startOrContinueApplication = userAction.async { implicit request =>
    val redirectWhenSaved = for {
      _         <- OptionT(save4LaterService.fetchBusinessRegistrationDetails(request.userId))
      _         <- OptionT(save4LaterService.fetchBusinessType(request.userId))
      savedDate <- OptionT(save4LaterService.fetchLastUpdateTime(request.userId))
    } yield {
      val expiryDate = dateTimeHelper.generateDate(27, savedDate)
      Ok(views.continue_delete(dateTimeHelper.convertDateToString(expiryDate), deleteOrContinueForm))
    }

    redirectWhenSaved getOrElseF newApplication
  }

  private def newApplication(implicit request: UserRequest[_]) =
    save4LaterService
      .fetch(request.userId)
      .map {
        case Some(_) => save4LaterService.removeUserData(request.userId)
        case None    =>
      }
      .map(_ => Redirect(links.businessCustomerVerificationUrl))

  def continueWithBpr = newApplicationAction.async { implicit request =>
    for {
      details <- businessCustomerConnector.getReviewDetails
      _       <- save4LaterService.saveBusinessRegistrationDetails(request.userId, details)
    } yield Redirect(routes.Application.businessType)
  }

  def deleteOrContinue(isNewForm: Boolean) = userAction.async { implicit request =>
    if (isNewForm) {
      Future successful Redirect(routes.Application.businessType)
    } else {
      save4LaterService.fetchLastUpdateTime(request.userId) map {
        case Some(savedDate) =>
          val expiryDate = dateTimeHelper.generateDate(27, savedDate)
          Ok(views.continue_delete(dateTimeHelper.convertDateToString(expiryDate), deleteOrContinueForm))
        case None => Redirect(routes.Application.businessType)
      }
    }
  }

  def submitDeleteOrContinue = userAction.async { implicit request =>
    deleteOrContinueForm
      .bindFromRequest()
      .fold(
        formWithErrors => Future successful errorHandler.errorResultsPages(Results.BadRequest),
        deleteOrContinue =>
          if (deleteOrContinue == "delete") {
            save4LaterService.fetchLastUpdateTime(request.userId) flatMap {
              case Some(savedDate) =>
                Future successful {
                  val date = dateTimeHelper.generateDate(0, savedDate)
                  Ok(views.confirm_delete(dateTimeHelper.convertDateToString(date)))
                }
              case None =>
                Future successful errorHandler.errorResultsPages(Results.BadRequest)
            }
          } else {
            Future successful Redirect(routes.Application.resumeForm)
          }
      )
  }

  def confirmDelete = userAction.async { implicit request =>
    save4LaterService.fetchLastUpdateTime(request.userId) flatMap {
      case Some(savedDate) =>
        Future successful {
          val date = dateTimeHelper.generateDate(0, savedDate)
          Ok(views.confirm_delete(dateTimeHelper.convertDateToString(date)))
        }
      case None => Future successful BadRequest
    }
  }

  def resumeForm = journeyAction { implicit request: JourneyRequest[AnyContent] =>
    if (request.journeyState.isComplete)
      Redirect(routes.SummaryController.summary())
    else {
      request.journeyState.lastEditedPage.map(p => p.id -> p.lastSection) match {
        case None =>
          val firstPage = request.journeyPages.pages.head.id
          Redirect(routes.FormPageController.load(firstPage))
        case Some((pid, Some(section))) => Redirect(routes.FormPageController.loadWithSection(pid, section))
        case Some((pid, None))          => Redirect(routes.FormPageController.load(pid))

      }
    }
  }

  def deleteUserData = userAction.async { implicit request =>
    save4LaterService.removeUserData(request.userId) map { _ =>
      Redirect(routes.Application.main())
    }
  }

  def businessType = userAction { implicit request: Request[AnyContent] =>
    Ok(views.business_type(businessTypeForm, links.businessCustomerVerificationUrl))
  }

  def submitBusinessType = userAction.async { implicit request =>
    businessTypeForm
      .bindFromRequest()
      .fold(
        formWithErrors =>
          Future.successful(BadRequest(views.business_type(formWithErrors, links.businessCustomerVerificationUrl))),
        businessType =>
          for {
            _ <- save4LaterService.saveBusinessType(request.userId, businessType)
          } yield Redirect(routes.EmailVerificationController.contactEmail)
      )
  }

  def startForm = userAction { implicit request: Request[AnyContent] =>
    Redirect(routes.Application.resumeForm)
  }

  def savedForLater = userAction.async { implicit request =>
    save4LaterService.fetchLastUpdateTime(request.userId).map {
      case Some(savedDate) =>
        val date = dateTimeHelper.generateDate(formMaxExpiryDays, savedDate)
        Ok(views.saved(dateTimeHelper.convertDateToString(date)))
      case None => errorHandler.errorResultsPages(Results.NotFound)
    }
  }

  def checkStatus() = enrolledUserAction.async { implicit request =>
    fhddsConnector
      .getStatus(request.registrationNumber)(hc)
      .map(fhddsStatus => Ok(status(statusParams.apply(fhddsStatus).get, request.registrationNumber)))
  }
}

@Singleton
abstract class AppController(val ds: CommonPlayDependencies, val cc: MessagesControllerComponents)
    extends FrontendController(cc) with I18nSupport with Logging {

  override implicit val messagesApi: MessagesApi = ds.messagesApi

  implicit val appConfig: AppConfig = ds.appConfig
  implicit val errorHandler: ErrorHandler = ds.errorHandler
  implicit val viewHelpers: ViewHelpers = ds.viewHelpers

  val configuration: Configuration = ds.conf
  val messages: MessagesApi = messagesApi
}

@Singleton
class CommonPlayDependencies @Inject() (
  val conf: Configuration,
  val appConfig: AppConfig,
  val env: Environment,
  val messagesApi: MessagesApi,
  val errorHandler: ErrorHandler,
  val viewHelpers: ViewHelpers
)
