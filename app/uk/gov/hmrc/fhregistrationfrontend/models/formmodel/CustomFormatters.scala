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

package uk.gov.hmrc.fhregistrationfrontend.models.formmodel

import play.api.data.FormError
import play.api.data.format.Formatter

object CustomFormatters {

  def yesOrNoFormatter(errorKey: String): Formatter[Boolean] = new Formatter[Boolean] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Boolean] =
      Right(data.getOrElse(key, "")).flatMap {
        case "true"  => Right(true)
        case "false" => Right(false)
        case _       => Left(Seq(FormError(key, errorKey)))
      }

    override def unbind(key: String, value: Boolean) = Map(key -> value.toString)
  }

  def transformNino(nino: String): String = nino.filterNot(_.isWhitespace).replaceAll("[,.!?\\-]", "").toUpperCase()

  def ninoFormatter(): Formatter[String] = new Formatter[String] {
    val ninoRegex =
      """^[ \t\/,.!?\\-]*[A-Za-z]{1}[ \t\/,.!?\\-]*[ \t\/,.!?\\-]*[A-Za-z]{1}[ \t\/,.!?\\-]*[0-9]{1}[ \t\/,.!?\\-]*[ \t\/,.!?\\-]*[0-9]{1}[ \t\/,.!?\\-]*[ \t\/,.!?\\-]*[0-9]{1}[ \t\/,.!?\\-]*[ \t\/,.!?\\-]*[0-9]{1}[ \t\/,.!?\\-]*[ \t\/,.!?\\-]*[0-9]{1}[ \t\/,.!?\\-]*[ \t\/,.!?\\-]*[0-9]{1}[ \t\/,.!?\\-]*[A-Da-d]{1}[ \t\/,.!?\\-]*$"""

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      Right(data.getOrElse(key, "")).flatMap {
        case ""                               => Left(Seq(FormError(key, "error.required")))
        case nino if nino.matches(ninoRegex)  => Right(transformNino(nino))
        case nino if !nino.matches(ninoRegex) => Left(Seq(FormError(key, "error.pattern")))
      }

    override def unbind(key: String, value: String) =
      Map(key -> value.toString)
  }

  def emailConfirmationFormat: Formatter[String] = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      Right(data.getOrElse(s"alternativeEmail.emailConfirmation", "")).flatMap {
        case "" => Left(Seq(FormError("alternativeEmail.emailConfirmation", "empty.error")))
        case emailConfirmation =>
          Right(data.getOrElse("alternativeEmail.email", "")).flatMap {
            case "" => Left(Seq(FormError("alternativeEmail.email", "empty.error")))
            case email =>
              if (email == emailConfirmation) Right(emailConfirmation)
              else Left(Seq(FormError("alternativeEmail.emailConfirmation", "no_match.error")))
          }
      }

    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value.toString)
  }

  def enumFormat[E <: Enumeration](`enum`: E, requiredErrorKey: String, args: Seq[String]): Formatter[E#Value] =
    new Formatter[E#Value] {
      def bind(key: String, data: Map[String, String]) =
        data.get(key) match {
          case None                      => Left(Seq(FormError(key, requiredErrorKey, args)))
          case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, requiredErrorKey, args)))
          case Some(s) =>
            scala.util.control.Exception
              .allCatch[E#Value]
              .either(`enum`.withName(s))
              .left
              .map(e => {
                Seq(FormError(key, "error.invalid", args))
              })
        }

      def unbind(key: String, value: E#Value) = Map(key -> value.toString)
    }

  def stringFormatter(errorKey: String, args: Seq[String] = Seq.empty): Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] =
      data.get(key) match {
        case None                      => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
        case Some(s)                   => Right(s)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

}
