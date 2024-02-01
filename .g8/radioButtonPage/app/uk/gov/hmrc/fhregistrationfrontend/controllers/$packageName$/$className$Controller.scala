package uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.controllers.{AppController, CommonPlayDependencies, ControllerHelper}
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$.$className$Form.form
import models.Mode
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$.$className$Page
import uk.gov.hmrc.fhregistrationfrontend.views.html.$packageName$.v2.$className$View

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

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

  //TODO: Update backUrl so it is the previous page of the section
  def backUrl(index: Int, mode: Mode): String = "#"

  def onPageLoad(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction$packageName;format = "cap" $ (index, mode) {
    implicit request =>

        val preparedForm = request.userAnswers.get($className$Page(index)) match {
            case None => form
            case Some(value) => form.fill(value)
        }

      Ok($className;format="decap"$View(preparedForm, postAction(index, mode), backUrl(index, mode)))
    }

  def onSubmit(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction$packageName;format = "cap" $ (index, mode).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(
            $className;format="decap"$View(formWithErrors, postAction(index, mode), backUrl(index, mode))
          )),

        value => {
          val updatedAnswers = request.userAnswers.set($className$Page(index), value)
          //Todo update startCall when doing navigation
          updateUserAnswersAndSaveToCache(updatedAnswers, startCall, $className$Page(index))
        }
     )
  }
}
