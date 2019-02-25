/*
 * Copyright 2019 HM Revenue & Customs
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
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, AlternativeEmail, InternationalAddress}
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.CustomFormatters._
import Constraints.oneOfConstraint
import org.apache.commons.lang3.StringUtils

import scala.util.Try

object Mappings {

  type Condition = Map[String, String] ⇒ Boolean

  val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def skippingOnePrefix[T](mapping: Mapping[T]) = new SkippingOnePrefixMapping(mapping)

  def yesOrNo = of(yesOrNoFormatter)

  def address: Mapping[Address] = mapping(
    "Line1" -> addressLine,
    "Line2" -> optional(addressLine),
    "Line3" -> optional(addressLine),
    "Line4" -> optional(addressLine),
    "postcode" -> postcode,
    "countryCode" -> optional(nonEmptyText),
    "lookupId" → optional(text).transform(_ filterNot StringUtils.isBlank, (v: Option[String]) ⇒ v)
  )(Address.apply)(Address.unapply)

  def postcode: Mapping[String] = nonEmptyText.verifying(Constraints.pattern("^[A-Za-z]{1,2}[0-9][0-9A-Za-z]?\\s?[0-9][A-Za-z]{2}$".r))
  def addressLine: Mapping[String] = nonEmptyText verifying Constraints.pattern("^[A-Za-z0-9 !'‘’\"“”(),./—–‐-]{1,35}$".r)
  def personTitle: Mapping[String] = nonEmptyText verifying Constraints.pattern("^[a-zA-ZÀ-ÿ '‘’—–‐-]{2,30}$".r)
  def roleInOrganization: Mapping[String] = nonEmptyText verifying Constraints.pattern("^[a-zA-Z &`\\-\\'^]{1,40}$".r)

  def personName: Mapping[String] = nonEmptyText verifying Constraints.pattern("^[a-zA-ZÀ-ÿ '‘’—–‐-]{1,30}$".r)
  def telephone: Mapping[String] = nonEmptyText verifying Constraints.pattern("^[0-9 ()+‐-]{7,24}$".r)
  def email: Mapping[String] = nonEmptyText(0, 100) verifying Constraints.emailAddress

  def companyRegistrationNumber = nonEmptyText verifying Constraints.pattern("^[A-Z0-9]{8}$".r)
  def vatRegistrationNumber = nonEmptyText verifying Constraints.pattern("^[0-9]{9}$".r)
  def companyName = nonEmptyText verifying Constraints.pattern("^[a-zA-Z0-9À-ÿ !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C—–‐-]{1,140}$".r)
  def tradingName = nonEmptyText verifying Constraints.pattern("^[a-zA-Z0-9À-ÿ !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C—–‐_^`-]{1,120}$".r)
  def eoriNumber = nonEmptyText verifying Constraints.pattern("^[A-Z0-9 -]{1,15}$".r)

  def uniqueTaxpayerReferenceNumber = nonEmptyText verifying Constraints.pattern("^[0-9]{10}$".r)

  def withdrwalReason = nonEmptyText verifying Constraints.pattern("^[a-zA-Z0-9 ]{1,40}$".r)
  def deregistrationReason= nonEmptyText verifying Constraints.pattern("^[a-zA-Z0-9 ]{1,40}$".r)
  def nino = nonEmptyText verifying Constraints
    .pattern("^((?!(BG|GB|KN|NK|NT|TN|ZZ)|(D|F|I|Q|U|V)[A-Z]|[A-Z](D|F|I|O|Q|U|V))[A-Z]{2})[0-9]{6}[A-D]$".r)
  def nationalIdNumber = nonEmptyText verifying Constraints.pattern("^[a-zA-Z0-9À-ÿ !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C—–‐_^`-]{1,20}$".r)
  def passportNumber = nonEmptyText verifying Constraints.pattern("^[a-zA-Z0-9À-ÿ !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C—–‐_^`-]{1,20}$".r)

  def alternativeEmail: Mapping[AlternativeEmail] = mapping(
    "email" → email,
    "emailConfirmation" → of(emailConfirmationFormat)
  )(AlternativeEmail.apply)(AlternativeEmail.unapply)

  def internationalAddress: Mapping[InternationalAddress] = mapping(
    "Line1" -> addressLine,
    "Line2" -> optional(addressLine),
    "Line3" -> optional(addressLine),
    "Line4" -> addressLine,
    "countryCode" -> nonEmptyText
  )(InternationalAddress.apply)(InternationalAddress.unapply)

  def localDate = tuple(
    "day" -> number(min = 1, max = 31),
    "month" -> number(min = 1, max = 12),
    "year" -> number(min = 1800, max = 2999)
  ) verifying ("error.invalid", x ⇒ localDateTimeConstraint(x)) transform (
    x ⇒ localDateTime(x),
    (d: LocalDate) ⇒ (d.getDayOfMonth, d.getMonth.getValue, d.getYear)
  )

  def localDateTime(d: (Int, Int, Int)) = {
    LocalDate.of(d._3, d._2, d._1)
  }

  def localDateTimeConstraint(d: (Int, Int, Int)) = {
    Try(localDateTime(d)).isSuccess
  }

  def oneOf(options: Seq[String]) = nonEmptyText verifying oneOfConstraint(options)
  def enum[E <: Enumeration](enum: E): Mapping[E#Value] = of(enumFormat(enum))

  def optionalWithYesOrNo[T](wrapped: Mapping[T]): Mapping[Option[T]] =
    x(wrapped) verifying("error.invalid", y) transform (z, t)

  private def x[T](wrapped: Mapping[T]): Mapping[(Boolean, Option[T])] = tuple(
    "yesNo" → of(yesOrNoFormatter),
    "value" → optional(wrapped)
  )


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
