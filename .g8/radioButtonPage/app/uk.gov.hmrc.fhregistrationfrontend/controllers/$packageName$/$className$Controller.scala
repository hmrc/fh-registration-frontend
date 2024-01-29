package uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$

import controllers.ControllerHelper
import uk.gov.hmrc.fhregistrationfrontend.actions._
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.forms.$packageName$.$className$FormProvider
import javax.inject.Inject
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$.$className$Page
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.views.Views
import uk.gov.hmrc.fhregistrationfrontend.Controllers
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$FormProvider
import scala.concurrent.{ExecutionContext, Future}

class $className$Controller @Inject()(
                                       ds: CommonPlayDependencies,
                                       view: Views,
                                       actions: Actions,
                                       formProvider: $className$FormProvider,
                                       val sessionCache: SessionRepository)(
                                       cc: MessagesControllerComponents
                                     )(implicit val ec: ExecutionContext)
  extends ControllerHelper {

  imports actions._

  val form = formProvider()

  def postAction(index: Int, mode: Mode): Call =
    routes.$className$Controller.next(index, mode)

  //TODO: Update backUrl so it is the previous page of the section
  def backUrl(index: Int, mode: Mode): String = "#"


  def onPageLoad(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction(index, mode) {
  implicit request =>
    val formData = request.userAnswers.get($className$Controller(index))
    val prepopulatedForm = formData.map(data => form.fill(data)).getOrElse(form)

      Ok(view.$packageName$Views.$className;format="decap"$(prepopulatedForm, postAction(index, mode), backUrl(index, mode))))
  }

  def onSubmit(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction(index, mode).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view.$packageName$Views.$className;format="decap"$(formWithErrors, postAction(index, mode)))),

        value => {
          val updatedAnswers = request.userAnswers.set($className$Page(index), value)
          //Todo: update startCall so it is the nextPage call
          updateUserAnswersAndSaveToCache(updatedAnswers, startCall, page)
        }
      )
  }
}
