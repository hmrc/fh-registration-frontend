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

import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc._
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrievals.internalId
import uk.gov.hmrc.auth.core.{NoActiveSession, _}
import uk.gov.hmrc.auth.otac.OtacFailureThrowable
import uk.gov.hmrc.fhregistrationfrontend.config.{ConcreteOtacAuthConnector, FrontendAuthConnector}
import uk.gov.hmrc.fhregistrationfrontend.connectors.ExternalUrls._
import uk.gov.hmrc.fhregistrationfrontend.connectors._
import uk.gov.hmrc.fhregistrationfrontend.models.businessregistration.BusinessRegistrationDetails
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.MainBusinessAddressForm._
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.ContactPersonForm._
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.CompanyRegistrationNumberForm._
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.DateOfIncorporationForm._
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.TradingNameForm._
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.VatNumberForm._
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.CompanyOfficersForm._
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService
import uk.gov.hmrc.fhregistrationfrontend.views.html.error_template_Scope0.error_template
import uk.gov.hmrc.fhregistrationfrontend.views.html.forms._
import uk.gov.hmrc.fhregistrationfrontend.views.html.ltd_summary
import uk.gov.hmrc.fhregistrationfrontend.views.html.registrationstatus._
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContextExecutor, Future}

@Singleton
class Application @Inject()(
  links            : ExternalUrls,
  ds               : CommonPlayDependencies,
  fhddsConnector   : FhddsConnector,
  messagesApi      : play.api.i18n.MessagesApi,
  configuration    : Configuration,
  save4LaterService: Save4LaterService
) extends AppController(ds, messagesApi) {

  val businessCustomerConnector = BusinessCustomerFrontendConnector

  val soleTraderFormTypeRef: String = configuration.getString(s"fhdds-dfs-frontend.fhdds-sole-proprietor").getOrElse("fhdds-sole-proprietor")
  val limitedCompanyFormTypeRef: String = configuration.getString(s"fhdds-dfs-frontend.fhdds-limited-company").getOrElse("fhdds-limited-company")
  val partnershipFormTypeRef: String = configuration.getString(s"fhdds-dfs-frontend.fhdds-partnership").getOrElse("fhdds-partnership")

  def whitelisted(p: String) = Action.async {
    implicit request ⇒
      authorised() {
        val verificationUrl = configuration.getString("services.verificationUrl").getOrElse("http://localhost:9227/verification/otac/login")
        Future successful Redirect(s"$verificationUrl?p=$p").withSession(request.session + (SessionKeys.redirect → routes.Application.start().url))
      } recover {
        case x: NoActiveSession ⇒
          Logger.warn(s"could not authenticate user due to: No Active Session " + x)

          val ggRedirectParms = Map(
            "continue" -> Seq(s"$continueUrl/whitelisted?p=$p"),
            "origin" -> Seq(getString("appName"))
          )
          Redirect(ggLoginUrl, ggRedirectParms)
      }
  }

  def start = ggAuthorised { implicit request ⇒
    Future.successful(Redirect(links.businessCustomerVerificationUrl))
  }

  def continue = authorisedUser { implicit request ⇒
    internalId ⇒
      for {
        details ← businessCustomerConnector.getReviewDetails
        _ ← save4LaterService.saveBusinessRegistrationDetails(internalId, details)
      } yield {
        Redirect(routes.Application.startApp())
      }
  }

  def summary = Action.async { implicit request ⇒

    Future.successful(Ok(ltd_summary("ok")))
  }

  def checkStatus(fhddsRegistrationNumber: String) = Action.async { implicit request ⇒
    fhddsConnector
      .getStatus(fhddsRegistrationNumber: String)(hc)
      .map(statusResp ⇒ {
        Ok(status(statusResp.body, fhddsRegistrationNumber))
      })
  }

  def startApp = authorisedUser { implicit request ⇒
    internalId ⇒
      save4LaterService.fetchBusinessRegistrationDetails(internalId) map {
        case Some(bpr) ⇒ Redirect(routes.Application.mainBusinessAddress())
        case None      ⇒ Redirect(links.businessCustomerVerificationUrl)
      }
  }

  def mainBusinessAddress = authorisedUser { implicit request ⇒
    internalId ⇒
      save4LaterService.fetchBusinessRegistrationDetails(internalId) map {
        case Some(bpr) ⇒ Ok(main_business_address(mainBusinessAddressForm, bpr))
        case None      ⇒ Redirect(links.businessCustomerVerificationUrl)
      }
  }

  def submitMainBusinessAddress = authorisedUser { implicit request =>
    internalId ⇒
      println(s"\n\n${mainBusinessAddressForm.bindFromRequest()}\n\n")
      save4LaterService.fetchBusinessRegistrationDetails(internalId) map {
        case Some(bpr) ⇒
          mainBusinessAddressForm.bindFromRequest().fold(
            formWithErrors => BadRequest(main_business_address(formWithErrors, bpr)),
            mainBusinessAddress => {
              Redirect(routes.Application.contactPerson())
            }
          )
        case None      ⇒ Redirect(links.businessCustomerVerificationUrl)
      }
  }

  def contactPerson = authorisedUser { implicit request ⇒
    internalId ⇒
      save4LaterService.fetchBusinessRegistrationDetails(internalId) map {
        case Some(bpr) ⇒ Ok(contact_person(contactPersonForm, bpr))
        case None      ⇒ Redirect(links.businessCustomerVerificationUrl)
      }
  }

  def submitContactPerson = authorisedUser { implicit request =>
    internalId ⇒
      println(s"\n\n${contactPersonForm.bindFromRequest()}\n\n")
      save4LaterService.fetchBusinessRegistrationDetails(internalId) map {
        case Some(bpr) ⇒
          contactPersonForm.bindFromRequest().fold(
            formWithErrors => BadRequest(contact_person(formWithErrors, bpr)),
            contactPerson => {
              Redirect(routes.Application.companyRegistrationNumber())
            }
          )
        case None      ⇒ Redirect(links.businessCustomerVerificationUrl)
      }
  }

  def companyRegistrationNumber = authorisedUser { implicit request ⇒
    internalId ⇒
      Future successful Ok(company_registration_number(companyRegistrationNumberForm))
  }

  def submitCompanyRegistrationNumber = authorisedUser { implicit request =>
    internalId ⇒
      println(s"\n\n${companyRegistrationNumberForm.bindFromRequest()}\n\n")
      companyRegistrationNumberForm.bindFromRequest().fold(
        formWithErrors => Future successful BadRequest(company_registration_number(formWithErrors)),
        companyRegistrationNumber => {
          Future successful Redirect(routes.Application.dateOfIncorporation())
        }
      )
  }

  def dateOfIncorporation = authorisedUser { implicit request ⇒
    internalId ⇒
      Future successful Ok(date_of_incorporation(dateOfIncorporationForm))
  }

  def submitDateOfIncorporation = authorisedUser { implicit request =>
    internalId ⇒
      println(s"\n\n${dateOfIncorporationForm.bindFromRequest()}\n\n")
      dateOfIncorporationForm.bindFromRequest().fold(
        formWithErrors => {
          Future successful BadRequest(date_of_incorporation(formWithErrors))},
        dateOfIncorporation => {
          Future successful Redirect(routes.Application.tradingName())
        }
      )
  }

  def tradingName = authorisedUser { implicit request ⇒
    internalId ⇒
      Future successful Ok(trading_name(tradingNameForm))
  }

  def submitTradingName = authorisedUser { implicit request =>
    internalId ⇒
      println(s"\n\n${tradingNameForm.bindFromRequest()}\n\n")
      tradingNameForm.bindFromRequest().fold(
        formWithErrors => {
          Future successful BadRequest(trading_name(formWithErrors))},
        tradingName => {
          Future successful Redirect(routes.Application.vatNumber())
        }
      )
  }

  def vatNumber = authorisedUser { implicit request ⇒
    internalId ⇒
      Future successful Ok(vat_registration(vatNumberForm))
  }

  def submitVatNumber = authorisedUser { implicit request =>
    internalId ⇒
      println(s"\n\n${vatNumberForm.bindFromRequest()}\n\n")
      vatNumberForm.bindFromRequest().fold(
        formWithErrors => {
          Future successful BadRequest(vat_registration(formWithErrors))},
        tradingName => {
          Future successful Redirect(routes.Application.companyOfficers())
        }
      )
  }

  def companyOfficers = authorisedUser { implicit request ⇒
    internalId ⇒
      Future successful Ok(company_officers(companyOfficersForm))
  }

  def submitCompanyOfficers = authorisedUser { implicit request =>
    internalId ⇒
      println(s"\n\n${companyOfficersForm.bindFromRequest()}\n\n")
      companyOfficersForm.bindFromRequest().fold(
        formWithErrors => {
          Future successful BadRequest(company_officers(formWithErrors))},
        tradingName => {
          Future successful Ok(companyOfficersForm.toString)
        }
      )
  }

  def componentExamples = Action.async { implicit request =>
    Future(Ok(examples()))
  }


  private def formTypeRef(details: BusinessRegistrationDetails) = {

    details.businessType match {
      case Some("Sole Trader")    ⇒ soleTraderFormTypeRef
      case Some("corporate body") ⇒ limitedCompanyFormTypeRef
      case Some("Partnership")    ⇒ partnershipFormTypeRef
      case _                      ⇒ limitedCompanyFormTypeRef
    }
  }

  override def usewhiteListing = configuration.getBoolean("services.whitelisting.enabled").getOrElse(false)
}

@Singleton
abstract class AppController(ds: CommonPlayDependencies, messages: play.api.i18n.MessagesApi)
  extends FrontendController
    with I18nSupport
    with AuthorisedFunctions
    with Whitelisting {

  implicit val executionContext: ExecutionContextExecutor = scala.concurrent.ExecutionContext.Implicits.global

  lazy val conf: Configuration = ds.conf
  implicit lazy val messagesApi: MessagesApi = ds.messagesApi

  override def authConnector: PlayAuthConnector = FrontendAuthConnector

  override def otacAuthConnector = ConcreteOtacAuthConnector

  lazy val authProvider: AuthProviders = AuthProviders(GovernmentGateway)
  val hasCtUtr: Predicate = Enrolment("IR-CT")

  def ggAuthorised(action: Request[AnyContent] ⇒ Future[Result]): Action[AnyContent] = {
    Action.async { implicit request ⇒
      withVerifiedPasscode("fhdds", request.session.get(SessionKeys.otacToken)) {
        authorised() {
          action(request)
        }
      } recover { case e ⇒ handleFailure(e) }
    }
  }


  def authorisedUser(action: Request[AnyContent] ⇒ String ⇒ Future[Result]): Action[AnyContent] = {
    Action.async { implicit request ⇒
      withVerifiedPasscode("fhdds", request.session.get(SessionKeys.otacToken)) {
        authorised().retrieve(internalId) {
          case Some(iid) ⇒ {
            action(request)(iid)
          }
          case None      ⇒ throw AuthorisationException.fromString("Can not find user id")
        } recover { case e ⇒ handleFailure(e) }
      }
    }
  }

  def handleFailure(e: Throwable)(implicit request: Request[_], messages: Messages): Result =
    e match {
      case x: NoActiveSession      ⇒
        Logger.warn(s"could not authenticate user due to: No Active Session " + x)

        val ggRedirectParms = Map(
          "continue" -> Seq(continueUrl),
          "origin" -> Seq(getString("appName"))
        )

        Redirect(ggLoginUrl, ggRedirectParms)
      case e: OtacFailureThrowable ⇒
        val errorTemplate = new error_template()
        Unauthorized(errorTemplate.render("Unauthorized", "Unauthorized", "You are not authorized to use this service", request, messages))
      case ex                      ⇒
        Logger.warn(s"could not authenticate user due to: $ex")
        BadRequest(s"$ex")
    }
}

@Singleton
final class CommonPlayDependencies @Inject()(val conf: Configuration, val env: Environment, val messagesApi: MessagesApi)

case class UnexpectedState(errorMsg: String, json: Option[JsValue] = None)

object UnexpectedState {
  implicit val invalidStateFormat: OFormat[UnexpectedState] = Json.format[UnexpectedState]
}
