package controllers.$packageName$

import controllers.actions._
import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.$packageName$.$className$View
import models.SelectChange.$packageName;format="cap"$

class $className$Controller @Inject()(
                                       override val messagesApi: MessagesApi,
                                        controllerActions: ControllerActions,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: $className$View
                                     ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] = controllerActions.withRequiredJourneyData($packageName;format="cap"$) {
    implicit request =>
      Ok(view())
  }
}
