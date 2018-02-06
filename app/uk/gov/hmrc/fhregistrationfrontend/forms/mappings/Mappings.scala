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

package uk.gov.hmrc.fhregistrationfrontend.forms.mappings

import java.time.LocalDate

import play.api.data.Forms._
import play.api.data.Mapping
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Address
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.CustomFormatters

object Mappings {

  type Condition = Map[String, String] ⇒ Boolean

  //TODO use regexes where applicable
  def address: Mapping[Address] = mapping(
    "addressLine1" -> nonEmptyText,
    "addressLine2" -> nonEmptyText,
    "addressLine3" -> optional(nonEmptyText),
    "addressLine4" -> optional(nonEmptyText),
    "postcode" -> nonEmptyText,
    "countryCode" -> optional(nonEmptyText)
  )(Address.apply)(Address.unapply)


  //TODO implement
  def internationalAddress: Mapping[Address] = ???

  def localDate: Mapping[LocalDate] = ???


  def optionalFromYesAndNo[T](wrapped: Mapping[T]): Mapping[Option[T]] =
    x(wrapped) verifying("todo.provide.a.value", y) transform (z, t)

  private def x[T](wrapped: Mapping[T]): Mapping[(Boolean, Option[T])] = mapping(
    "yesNo" → of(CustomFormatters.radioButton),
    "value" → optional(wrapped)
  )(Tuple2.apply)(Tuple2.unapply)


  private def y[T]: ((Boolean, Option[T])) ⇒ Boolean = {
    case (true, Some(_)) ⇒ true
    case (false, None)   ⇒ true
    case _               ⇒ false
  }


  private def z[T]: ((Boolean, Option[T])) ⇒ Option[T] = {
    case (_, value) ⇒ value
  }

  private def t[T]: Option[T] ⇒ (Boolean, Option[T]) = { value ⇒
    (value.isDefined, value)
  }


}
