/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.hmrc.fhregistrationfrontend.connectors._
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.RecordSet
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupController @Inject()(
  addressLookupConnector: AddressLookupConnector,
  cc: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendController(cc) {

  def addressLookup(postcode: String, filter: Option[String]) = Action.async { implicit request =>
    implicit val writes = Json.format[RecordSet]
    val validPostcodeCharacters = "^[A-Za-z]{1,2}[0-9][0-9A-Za-z]?\\s?[0-9][A-Za-z]{2}$"
    if (postcode.matches(validPostcodeCharacters)) {
      addressLookupConnector.lookup(postcode, filter) map {
        case AddressLookupErrorResponse(e: BadRequestException) => BadRequest(e.message)
        case AddressLookupErrorResponse(e)                      => BadGateway
        case AddressLookupSuccessResponse(recordSet)            => Ok(writes.writes(recordSet))
      }
    } else {
      Future.successful(BadRequest("missing or badly-formed postcode parameter"))
    }
  }
}
