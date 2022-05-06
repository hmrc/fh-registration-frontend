/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.models.formmodel

import play.api.data.FormError
import play.api.data.format.Formatter

object CustomFormatters {

  val yesOrNoFormatter: Formatter[Boolean] = new Formatter[Boolean] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Boolean] =
      Right(data.getOrElse(key, "")).right.flatMap {
        case "true"  => Right(true)
        case "false" => Right(false)
        case _       => Left(Seq(FormError(key, "error.required")))
      }

    override def unbind(key: String, value: Boolean) = Map(key -> value.toString)
  }

  def emailConfirmationFormat: Formatter[String] = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      Right(data.getOrElse(s"alternativeEmail.emailConfirmation", "")).right.flatMap {
        case "" => Left(Seq(FormError("alternativeEmail.emailConfirmation", "empty.error")))
        case emailConfirmation =>
          Right(data.getOrElse("alternativeEmail.email", "")).right.flatMap {
            case "" => Left(Seq(FormError("alternativeEmail.email", "empty.error")))
            case email =>
              if (email == emailConfirmation) Right(emailConfirmation)
              else Left(Seq(FormError("alternativeEmail.emailConfirmation", "no_match.error")))
          }
      }

    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value.toString)
  }

  def enumFormat[E <: Enumeration](enum: E): Formatter[E#Value] = new Formatter[E#Value] {
    def bind(key: String, data: Map[String, String]) =
      play.api.data.format.Formats.stringFormat.bind(key, data).right.flatMap { s =>
        scala.util.control.Exception
          .allCatch[E#Value]
          .either(enum.withName(s))
          .left
          .map(e => Seq(FormError(key, "error.invalid", Nil)))
      }

    def unbind(key: String, value: E#Value) = Map(key -> value.toString)
  }

}
