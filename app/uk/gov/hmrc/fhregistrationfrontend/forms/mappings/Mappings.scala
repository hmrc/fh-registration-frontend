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

package uk.gov.hmrc.fhregistrationfrontend.forms.mappings

import org.apache.commons.lang3.StringUtils
import play.api.data.Forms._
import play.api.data.{FieldMapping, Mapping}
import play.api.data.validation._
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Constraints.oneOfConstraint
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, AlternativeEmail, InternationalAddress}
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.CustomFormatters._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

object Mappings {

  type Condition = Map[String, String] => Boolean

  val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def skippingOnePrefix[T](mapping: Mapping[T]) = new SkippingOnePrefixMapping(mapping)

  def yesOrNo(errorKey: String = "error.required") = of(yesOrNoFormatter(errorKey))

  def address: Mapping[Address] =
    mapping(
      "Line1"       -> addressLine,
      "Line2"       -> optional(addressLine),
      "Line3"       -> optional(addressLine),
      "Line4"       -> optional(addressLine),
      "postcode"    -> postcode,
      "countryCode" -> optional(nonEmptyText),
      "lookupId"    -> optional(text).transform(_ filterNot StringUtils.isBlank, (v: Option[String]) => v)
    )(Address.apply)(Address.unapply)

  def postcode: Mapping[String] =
    nonEmptyText.verifying(Constraints.pattern("^[A-Za-z]{1,2}[0-9][0-9A-Za-z]?\\s*?[0-9][A-Za-z]{2}$".r))
  def addressLine: Mapping[String] =
    nonEmptyText verifying Constraints.pattern("^[A-Za-z0-9 &!'‘’\"“”(),./\u2014\u2013\u2010\u002d]{1,35}$".r)
  def personTitle: Mapping[String] =
    nonEmptyText verifying Constraints.pattern("^[a-zA-Z\u00C0-\u00FF '‘’\u2014\u2013\u2010\u002d]{2,30}$".r)
  def roleInOrganization: Mapping[String] = nonEmptyText verifying Constraints.pattern("^[a-zA-Z &`\\-\\'^]{1,40}$".r)

  def personName: Mapping[String] =
    nonEmptyText verifying Constraints.pattern("^[a-zA-Z\u00C0-\u00FF '‘’\u2014\u2013\u2010\u002d]{1,35}$".r)
  def telephone: Mapping[String] = nonEmptyText verifying Constraints.pattern("^[0-9 ()+\u2010\u002d]{1,24}$".r)
  def email: Mapping[String] = nonEmptyText(0, 100) verifying Constraints.emailAddress

  def companyRegistrationNumber =
    nonEmptyText transform (value => value.replaceAll("\\s", ""), { v: String =>
      v
    }) verifying Constraints.pattern("^[a-zA-Z0-9]{8}$".r)
  def companyRegistrationNumberFormatted =
    nonEmptyText transform (value => value.trim, { v: String =>
      v
    })

  def vatRegistrationNumber = nonEmptyText verifying Constraints.pattern("^[0-9]{9}$".r)
  def companyName =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]£€¥\\u005C\u2014\u2013\u2010\u002d]{1,140}$".r)
  def tradingName =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,120}$".r)
  def unincorporatedBodyName =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,120}$".r)

  def ltdLiabilityPartnershipName =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,120}$".r)

  def partnershipName: Mapping[String] =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,120}$".r)
  def eoriNumber = nonEmptyText verifying Constraints.pattern("^[A-Z0-9 -]{1,15}$".r)

  def uniqueTaxpayerReferenceNumber = nonEmptyText verifying Constraints.pattern("^[0-9]{10}$".r)

  def withdrwalReason = nonEmptyText verifying Constraints.pattern("^[a-zA-Z0-9 ]{1,40}$".r)
  def deregistrationReason = nonEmptyText verifying Constraints.pattern("^[a-zA-Z0-9 ]{1,40}$".r)
  def nino = of(ninoFormatter())
  def nationalIdNumber =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,20}$".r)
  def passportNumber =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,20}$".r)

  def alternativeEmail: Mapping[AlternativeEmail] =
    mapping(
      "email"             -> email,
      "emailConfirmation" -> of(emailConfirmationFormat)
    )(AlternativeEmail.apply)(AlternativeEmail.unapply)

  def internationalAddress: Mapping[InternationalAddress] =
    mapping(
      "Line1"       -> addressLine,
      "Line2"       -> optional(addressLine),
      "Line3"       -> optional(addressLine),
      "Line4"       -> addressLine,
      "countryCode" -> nonEmptyText
    )(InternationalAddress.apply)(InternationalAddress.unapply)

  private type RawFormValues = (String, String, String)

  private def invalid(error: String, params: String*) =
    Invalid(
      Seq(
        ValidationError(error, params: _*)
      )
    )

  private def localDateFromValues(d: String, m: String, y: String) = Try(LocalDate.of(y.toInt, m.toInt, d.toInt))

  private val allDateValuesEntered: RawFormValues => ValidationResult = {
    case ("", "", "") => invalid("date.empty.error")
    case ("", "", _)  => invalid("day-and-month.missing")
    case (_, "", "")  => invalid("month-and-year.missing")
    case ("", _, "")  => invalid("day-and-year.missing")
    case ("", _, _)   => invalid("day.missing")
    case (_, "", _)   => invalid("month.missing")
    case (_, _, "")   => invalid("year.missing")
    case _            => Valid
  }

  private val dateIsValid: RawFormValues => ValidationResult = {
    case (d, m, y) if Try(s"$d$m$y".toInt).isFailure         => invalid("date.error.invalid")
    case (d, m, y) if localDateFromValues(d, m, y).isFailure => invalid("date.error.invalid")
    case _                                                   => Valid
  }

  private val dateInAllowedRange: RawFormValues => ValidationResult = {
    case (d, m, y) =>
      localDateFromValues(d, m, y)
        .map { parsedDate =>
          val enteredYear = parsedDate.getYear
          if (enteredYear >= 1800 && enteredYear <= 2999) Valid
          else
            invalid("date.error.invalid")
        }
        .getOrElse(Valid)
    case _ => Valid
  }

  def localNew =
    tuple(
      "day"   -> text,
      "month" -> text,
      "year"  -> text
    ).transform({ case (d, m, y) => (d.trim, m.trim, y.trim) }, { v: RawFormValues =>
        v
      })
      .verifying(Constraint(allDateValuesEntered(_)))
      .verifying(Constraint(dateIsValid(_)))
      .verifying(Constraint(dateInAllowedRange(_)))
      .transform(
        { case (d, m, y) => LocalDate.of(y.toInt, m.toInt, d.toInt) },
        (d: LocalDate) => (d.getDayOfMonth.toString, d.getMonthValue.toString, d.getYear.toString)
      )

  def localDate =
    tuple(
      "day"   -> number(min = 1, max = 31),
      "month" -> number(min = 1, max = 12),
      "year"  -> number(min = 1800, max = 2999)
    ) verifying ("error.invalid", x => localDateTimeConstraint(x)) transform (
      x => localDateTime(x),
      (d: LocalDate) => (d.getDayOfMonth, d.getMonth.getValue, d.getYear)
    )

  def localDateTime(d: (Int, Int, Int)) =
    LocalDate.of(d._3, d._2, d._1)

  def localDateTimeConstraint(d: (Int, Int, Int)) =
    Try(localDateTime(d)).isSuccess

  def oneOf(options: Seq[String]) = nonEmptyText verifying oneOfConstraint(options)
  def `enum`[E <: Enumeration](
    `enum`: E,
    requiredErrorKey: String = "error.required",
    args: Seq[String] = Nil): Mapping[E#Value] = of(enumFormat(`enum`, requiredErrorKey, args))

  def optionalWithYesOrNo[T](wrapped: Mapping[T]): Mapping[Option[T]] =
    x(wrapped) verifying ("error.invalid", y) transform (z, t)

  def string(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args))
  private def x[T](wrapped: Mapping[T]): Mapping[(Boolean, Option[T])] = tuple(
    "yesNo" -> of(yesOrNoFormatter("error.required")),
    "value" -> optional(wrapped)
  )

  private def y[T]: ((Boolean, Option[T])) => Boolean = {
    case (true, Some(_)) => true
    case (false, None)   => true
    case _               => false
  }

  private def z[T]: ((Boolean, Option[T])) => Option[T] = {
    case (_, value) => value
  }

  private def t[T]: Option[T] => (Boolean, Option[T]) = { value =>
    (value.isDefined, value)
  }

}
