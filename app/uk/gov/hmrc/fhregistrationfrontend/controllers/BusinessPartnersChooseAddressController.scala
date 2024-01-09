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

import com.google.inject.{Inject, Singleton}
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.actions.Actions
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.BusinessPartnersChooseAddressForm.chooseAddressForm
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, ChooseAddress}
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.AddressPage
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import models.{Mode, NormalMode}
import play.api.data.Form
import scalaz.Digit._2
import uk.gov.hmrc.fhregistrationfrontend.views.Views

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessPartnersChooseAddressController @Inject()(
  ds: CommonPlayDependencies,
  view: Views,
  actions: Actions,
  config: FrontendAppConfig,
  val sessionCache: SessionRepository)(
  cc: MessagesControllerComponents
)(implicit val ec: ExecutionContext)
    extends AppController(ds, cc) with ControllerHelper {
  import actions._

  private def getBusinessType: String = config.getRandomBusinessType()

  val backUrl: String = {
    if (getBusinessType == "unincorporated-body")
      routes.BusinessPartnersUnincorporatedBodyRegisteredAddressController.load().url
    else "#"
  }

  def postAction(index: Int, mode: Mode): Call = routes.BusinessPartnersChooseAddressController.next(index, mode)

  def load(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction { implicit request =>
    val formData = request.userAnswers.get(AddressPage(index))
    val prepopulatedForm =
      formData
        .flatMap { data =>
          testAddressData.find(_._2 == data).map(addressPair => chooseAddressForm.fill(ChooseAddress(addressPair._1)))
        }
        .getOrElse(chooseAddressForm)
    Ok(
      view.business_partners_choose_address(
        prepopulatedForm,
        postAction(index, mode),
        testAddressData,
        backUrl
      )
    )
  }

  def next(index: Int, mode: Mode): Action[AnyContent] = dataRequiredAction.async { implicit request =>
    chooseAddressForm
      .bindFromRequest()
      .fold(
        formWithErrors => {
          Future.successful(
            BadRequest(
              view.business_partners_choose_address(
                formWithErrors,
                postAction(index, mode),
                testAddressData,
                backUrl
              )
            )
          )
        },
        addressKey => {
          val page = AddressPage(index)
          val nextPage = routes.BusinessPartnersCheckYourAnswersController.load()

          val updatedUserAnswers = request.userAnswers.set(page, testAddressData.head._2)
          updateUserAnswersAndSaveToCache(updatedUserAnswers, nextPage, page)
        }
      )
  }

  //ToDo remove when addressData stored in database
  private val testAddressData: Map[String, Address] = {
    val address1 = Address(
      addressLine1 = "1 Romford Road",
      addressLine2 = Some("Wellington"),
      addressLine3 = Some("Telford"),
      addressLine4 = None,
      postcode = "TF1 4ER",
      countryCode = None,
      lookupId = None
    )

    val address2 = address1.copy(addressLine1 = "2 Romford Road")
    val address3 = address1.copy(addressLine1 = "3 Romford Road")
    val address4 = address1.copy(addressLine1 = "2 Romford Road")
    val address5 = address1.copy(addressLine1 = "5 Romford Road")

    Map(
      "1" -> address1,
      "2" -> address2,
      "3" -> address3,
      "4" -> address4,
      "5" -> address5
    )
  }

}
