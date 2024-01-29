package controllers.$packageName$

import utilities.GenericLogger
import controllers.ControllerHelper
import controllers.actions._
import forms.$packageName$.$className$FormProvider
import javax.inject.Inject
import models.Mode
import models.SelectChange.$packageName;format="cap"$
import pages.$packageName$.$className$Page
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionService
import views.html.$packageName$.$className$View
import handlers.ErrorHandler

import scala.concurrent.{ExecutionContext, Future}
import navigation._

class $className$Controller @Inject()(
                                       override val messagesApi: MessagesApi,
                                       val sessionService: SessionService,
                                       val navigator: NavigatorFor$packageName;format="cap"$,
                                        controllerActions: ControllerActions,
                                       formProvider: $className$FormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: $className$View,
                                       val genericLogger: GenericLogger,
                                       val errorHandler: ErrorHandler
                                     )(implicit ec: ExecutionContext) extends ControllerHelper {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData($packageName;format="cap"$) {
    implicit request =>

      val preparedForm = request.userAnswers.get($className$Page) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = controllerActions.withRequiredJourneyData($packageName;format="cap"$).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),

        value => {
          val updatedAnswers = request.userAnswers.set($className$Page, value)
          updateDatabaseAndRedirect(updatedAnswers, $className$Page, mode)
        }
      )
  }
}
