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
import java.time.format.DateTimeFormatter

import play.api.data.Forms._
import play.api.data.Mapping
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, InternationalAddress}
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.CustomFormatters

import scala.util.Try

object Mappings {

  type Condition = Map[String, String] ⇒ Boolean

  val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  //TODO use regexes where applicable
  def address: Mapping[Address] = mapping(
    "Line1" -> nonEmptyText,
    "Line2" -> nonEmptyText,
    "Line3" -> optional(nonEmptyText),
    "Line4" -> optional(nonEmptyText),
    "postcode" -> nonEmptyText,
    "countryCode" -> optional(nonEmptyText)
  )(Address.apply)(Address.unapply)

  def internationalAddress: Mapping[InternationalAddress] = mapping(
    "Line1" -> nonEmptyText,
    "Line2" -> optional(nonEmptyText),
    "Line3" -> optional(nonEmptyText),
    "Line4" -> nonEmptyText
  )(InternationalAddress.apply)(InternationalAddress.unapply)

  def localDate = tuple(
    "day" -> number(min = 1, max = 31),
    "month" -> number(min = 1, max = 12),
    "year" -> number(min = 1900, max = 2999)
  ) verifying ("invalid.date", x ⇒ localDateTimeConstraint(x)) transform (
    x ⇒ localDateTime(x),
    (d: LocalDate) ⇒ (d.getDayOfMonth, d.getMonth.getValue, d.getYear)
  )

  def localDateTime(d: (Int, Int, Int)) = {
    LocalDate.of(d._1, d._2, d._3)
  }

  def localDateTimeConstraint(d: (Int, Int, Int)) = {
    Try(localDateTime(d)).isSuccess
  }

  //LocalDate.parse(panelProposedStartDate.proposedStartDate, dtf)

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
