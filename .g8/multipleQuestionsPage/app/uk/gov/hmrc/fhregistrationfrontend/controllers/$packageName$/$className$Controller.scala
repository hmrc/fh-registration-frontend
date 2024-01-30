package uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.controllers.{CommonPlayDependencies, ControllerHelper}
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$.$className$Form.form
import models.Mode
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$.$className$Page
import uk.gov.hmrc.fhregistrationfrontend.views.html.$packageName$.$className$View

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class $className$Controller @Inject()(ds: CommonPlayDependencies,
                                      $className;format="decap"$View: $className$View,
                                      actions: Actions,
                                      val sessionCache: SessionRepository,
                                      cc: MessagesControllerComponents)
                                     (implicit val ec: ExecutionContext)
  extends AppController(ds, cc) with ControllerHelper {

  import actions._
  def postAction(index: Int, mode: Mode): Call =
    routes.$className$Controller.onSubmit(index, mode)

  //Todo populate with the back url when available
  def backUrl(index: Int, mode: Mode): String = "#"


  def onPageLoad(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction(index, mode) {
    implicit request =>

      val formData = request.userAnswers.get($className$Page(index))
      val prepopulatedForm = formData.map(data => form.fill(data)).getOrElse(form)

      Ok(view.$packageName$Views.$className;format="decap"$(prepopulatedForm, postAction(index, mode), backUrl(index, mode)))
  }

  def onSubmit(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction(index, mode).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(
            view.$packageName$Views.$className;format="decap"$(formWithErrors, postAction(index, mode), backUrl(index, mode))
          )),

        value => {
          val updatedUserAnswers = request.userAnswers.set($className$Page(index), value)
          //Todo update startCall so it is the nextPage Call
          updateUserAnswersAndSaveToCache(updatedUserAnswers, startCall, $className$Page(index))
        }
      )
  }
}
