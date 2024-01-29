package uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$

import controllers.ControllerHelper
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$.$className$FormProvider
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$.$className$Page
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$
import uk.gov.hmrc.fhregistrationfrontend.controllers

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

class $className$Controller @Inject()(ds: CommonPlayDependencies,
                                      view: Views,
                                      actions: Actions,
                                      formProvider: $className$FormProvider,
                                      val sessionCache: SessionRepository,
                                      cc: MessagesControllerComponents)
                                     (implicit val ec: ExecutionContext)
  extends ControllerHelper {

  import actions._

  val form = formProvider()

  def postAction(index: Int, mode: Mode): Call =
    routes.$className$Controller.next(index, mode)

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
            view.$packageName$Views.$className;format="decap"$(prepopulatedForm, postAction(index, mode), backUrl(index, mode))
          ),

        value => {
          val updatedAnswers = request.userAnswers.set($className$Page(index), value)
          //Todo update startCall so it is the nextPage Call
          updateUserAnswersAndSaveToCache(updatedUserAnswers, startCall, page)
        }
      )
  }
}
