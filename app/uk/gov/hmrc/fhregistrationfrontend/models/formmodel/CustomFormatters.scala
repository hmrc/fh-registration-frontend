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

package uk.gov.hmrc.fhregistrationfrontend.models.formmodel

import play.api.data.FormError
import play.api.data.format.Formatter

object CustomFormatters {

  val radioButton: Formatter[Boolean] = new Formatter[Boolean] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Boolean] = {
      Right(data.getOrElse(key, "")).right.flatMap {
        case "true"  => Right(true)
        case "false" => Right(false)
        case _       => Left(Seq(FormError(key, "confirm.selectone")))
      }
    }

    override def unbind(key: String, value: Boolean) = Map(key -> value.toString)
  }

  def requiredRadioButton(requiredKey: String, condition: String): Formatter[Option[Boolean]] = new Formatter[Option[Boolean]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[Boolean]] = {
      if (data.getOrElse(requiredKey, "") == condition) {
        Right(data.getOrElse(key, "")).right.flatMap {
          case "true"  => Right(Some(true))
          case "false" => Right(Some(false))
          case _       => Left(Seq(FormError(key, "confirm.selectone")))
        }
      } else {
        Right(None)
      }
    }

    override def unbind(key: String, value: Option[Boolean]) = Map(key -> value.toString)
  }

  def businessTypeCheck(businessTypeFromGGId: String): Formatter[String] = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      Right(data.getOrElse(key, "")).right.flatMap { businessType ⇒
        if (businessType == businessTypeFromGGId) Right(businessType)
        else if (businessType.isEmpty) Left(Seq(FormError(key, s"")))
        else Left(Seq(FormError(key, s"fh.business_type.type_not_match")))
      }
    }

    override def unbind(key: String, value: String) = Map(key -> value)
  }

}
