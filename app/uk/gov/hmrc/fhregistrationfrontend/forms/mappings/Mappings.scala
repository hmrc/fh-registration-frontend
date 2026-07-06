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
import play.api.data.Forms.*
import play.api.data.{FieldMapping, FormError, Mapping}
import play.api.data.validation.*
import uk.gov.hmrc.fhregistrationfrontend.forms.definitions.MainBusinessAddressForm.previousAddressStartdateKey
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Constraints.oneOfConstraint
import uk.gov.hmrc.fhregistrationfrontend.forms.models.{Address, AlternativeEmail, InternationalAddress}
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.CustomFormatters.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Try

object Mappings {

  type Condition = Map[String, String] => Boolean

  val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

  def skippingOnePrefix[T](mapping: Mapping[T]) = new SkippingOnePrefixMapping(mapping)

  def yesOrNo(errorKey: String = "error.required") = of(using yesOrNoFormatter(errorKey))

  def address: Mapping[Address] =
    mapping(
      "Line1"       -> addressLine,
      "Line2"       -> optional(addressLine),
      "Line3"       -> optional(addressLine),
      "Line4"       -> optional(addressLine),
      "postcode"    -> postcode,
      "countryCode" -> optional(nonEmptyText),
      "lookupId"    -> optional(text).transform(_ filterNot StringUtils.isBlank, (v: Option[String]) => v)
    )(Address.apply)(o => Some(Tuple.fromProductTyped(o)))

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

  def changedEmail(candidateEmail: Option[String]): Mapping[String] = {
    def checkCandidateEmail(): Constraint[String] =
      Constraint {
        case email if candidateEmail.contains(email) =>
          Invalid("error.emailAlreadyUsed")
        case _ =>
          Valid
      }
    email verifying checkCandidateEmail()
  }

  def companyRegistrationNumber: Mapping[String] =
    nonEmptyText transform (value => value.replaceAll("\\s", "").toUpperCase(), (v: String) =>
      v) verifying Constraints.pattern("^[A-Z0-9]{8}$".r)

  def companyRegistrationNumberFormatted: Mapping[String] =
    nonEmptyText transform (value => value.trim, (v: String) => v)

  def vatRegistrationNumber: Mapping[String] =
    nonEmptyText transform (value => value.replaceAll("\\s", ""), (v: String) => v) verifying Constraints.pattern(
      "^[a-zA-Z]{2}[0-9]{9}|[0-9]{9}$".r
    )

  def companyName: Mapping[String] =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]£€¥\\u005C\u2014\u2013\u2010\u002d]{1,140}$".r
    )

  def tradingName: Mapping[String] =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,120}$".r
    )

  def unincorporatedBodyName: Mapping[String] =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,120}$".r
    )

  def ltdLiabilityPartnershipName: Mapping[String] =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,120}$".r
    )

  def partnershipName: Mapping[String] =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,120}$".r
    )

  def eoriNumber: Mapping[String] =
    nonEmptyText transform (value => value.replaceAll("\\s", "").toUpperCase(), (v: String) =>
      v) verifying Constraints.pattern("^[A-Z0-9 -]{1,15}$".r)

  def uniqueTaxpayerReferenceNumber: Mapping[String] = nonEmptyText verifying Constraints.pattern("^[0-9]{10}$".r)

  def withdrwalReason: Mapping[String] = nonEmptyText verifying Constraints.pattern("^[a-zA-Z0-9 ]{1,40}$".r)

  def deregistrationReason: Mapping[String] = nonEmptyText verifying Constraints.pattern("^[a-zA-Z0-9 ]{1,40}$".r)

  def nino: FieldMapping[String] = of(using ninoFormatter())

  def nationalIdNumber: Mapping[String] =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,20}$".r
    )

  def passportNumber: Mapping[String] =
    nonEmptyText verifying Constraints.pattern(
      "^[a-zA-Z0-9\u00C0-\u00FF !#$%&'‘’\"“”«»()*+,./:;=?@\\[\\]|~£€¥\\u005C\u2014\u2013\u2010\u005F\u005E\u0060\u002d]{1,20}$".r
    )

  def alternativeEmail: Mapping[AlternativeEmail] =
    mapping(
      "email"             -> email,
      "emailConfirmation" -> of(using emailConfirmationFormat)
    )(AlternativeEmail.apply)(o => Some(Tuple.fromProductTyped(o)))

  def internationalAddress: Mapping[InternationalAddress] =
    mapping(
      "Line1"       -> addressLine,
      "Line2"       -> optional(addressLine),
      "Line3"       -> optional(addressLine),
      "Line4"       -> addressLine,
      "countryCode" -> nonEmptyText
    )(InternationalAddress.apply)(o => Some(Tuple.fromProductTyped(o)))

  private type RawFormValues = (String, String, String)

  private def invalid(error: String, fields: String*) =
    Invalid(
      fields.map(f => ValidationError(error, Seq.empty, Seq(s"$previousAddressStartdateKey.$f")))
    )

  private def localDateFromValues(d: String, m: String, y: String) = Try(LocalDate.of(y.toInt, m.toInt, d.toInt))

  private val allDateValuesEntered: RawFormValues => ValidationResult = {
    case ("", "", "") => invalid("date.empty.error", "day", "month", "year")
    case ("", "", _)  => invalid("day-and-month.missing", "day", "month")
    case (_, "", "")  => invalid("month-and-year.missing", "month", "year")
    case ("", _, "")  => invalid("day-and-year.missing", "day", "year")
    case ("", _, _)   => invalid("day.missing", "day")
    case (_, "", _)   => invalid("month.missing", "month")
    case (_, _, "")   => invalid("year.missing", "year")
    case _            => Valid
  }

  private val dateIsValid: RawFormValues => ValidationResult = {
    case (d, m, y) if d.isEmpty || m.isEmpty || y.isEmpty =>
      Valid

    case (d, m, y) =>
      val dayErrors =
        if (d.forall(_.isDigit)) Option.when(d.toInt < 1 || d.toInt > 31)("day")
        else Some("day")

      val monthErrors =
        if (m.forall(_.isDigit)) Option.when(m.toInt < 1 || m.toInt > 12)("month")
        else Some("month")

      val yearErrors =
        if (y.forall(_.isDigit)) Option.when(y.toInt < 1800 || y.toInt > 2999)("year")
        else Some("year")

      val errors = Seq(dayErrors, monthErrors, yearErrors).flatten

      if (errors.size == 1)
        invalid(s"date.error.invalid.${errors.head}", errors *)
      else if (errors.nonEmpty)
        invalid("date.error.invalid", errors *)
      else if (localDateFromValues(d, m, y).isFailure)
        invalid("date.error.invalid", "day", "month", "year")
      else
        Valid
  }

  private val dateInAllowedRange: RawFormValues => ValidationResult = {
    case (d, m, y) =>
      localDateFromValues(d, m, y)
        .map { parsedDate =>
          val enteredYear = parsedDate.getYear
          if (enteredYear >= 1800 && enteredYear <= 2999) Valid
          else
            invalid("date.error.invalid", "year")
        }
        .getOrElse(Valid)
    case _ => Valid
  }

  val dateInPast: RawFormValues => ValidationResult = {
    case (d, m, y) =>
      localDateFromValues(d, m, y)
        .map { parsedDate =>
          if (parsedDate.isAfter(LocalDate.now())) invalid("date.error.inPast", "day", "month", "year")
          else Valid
        }
        .getOrElse(Valid)
    case _ => Valid
  }

  private def baseDateMapping: Mapping[RawFormValues] =
    tuple(
      "day"   -> text,
      "month" -> text,
      "year"  -> text
    ).transform(
      { case (d, m, y) =>
        (d.trim, m.trim, y.trim)
      },
      (v: RawFormValues) => v
    ).verifying(Constraint(allDateValuesEntered(_)))
      .verifying(Constraint(dateIsValid(_)))
      .verifying(Constraint(dateInAllowedRange(_)))

  def localNew: Mapping[LocalDate] =
    baseDateMapping.transform(
      { case (d, m, y) => LocalDate.of(y.toInt, m.toInt, d.toInt) },
      (d: LocalDate) => (d.getDayOfMonth.toString, d.getMonthValue.toString, d.getYear.toString)
    )

  def localNewInPast: Mapping[LocalDate] =
    baseDateMapping
      .verifying(Constraint(dateInPast(_)))
      .transform(
        { case (d, m, y) =>
          LocalDate.of(y.toIntOption.getOrElse(1900), m.toIntOption.getOrElse(1), d.toIntOption.getOrElse(1))
        },
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

  private def localDateTime(d: (Int, Int, Int)) =
    LocalDate.of(d._3, d._2, d._1)

  private def localDateTimeConstraint(d: (Int, Int, Int)) =
    Try(localDateTime(d)).isSuccess

  def oneOf(options: Seq[String]): Mapping[String] = nonEmptyText verifying oneOfConstraint(options)

  def `enum`[E <: Enumeration](
    `enum`: E,
    requiredErrorKey: String = "error.required",
    args: Seq[String] = Nil
  ): Mapping[`enum`.Value] =
    of(using enumFormat(`enum`, requiredErrorKey, args))

  def optionalWithYesOrNo[T](wrapped: Mapping[T]): Mapping[Option[T]] =
    x(wrapped) verifying ("error.invalid", y) transform (z, t)

  def string(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(using stringFormatter(errorKey, args))

  private def x[T](wrapped: Mapping[T]): Mapping[(Boolean, Option[T])] = tuple(
    "yesNo" -> of(using yesOrNoFormatter("error.required")),
    "value" -> optional(wrapped)
  )

  private def y[T]: ((Boolean, Option[T])) => Boolean = {
    case (true, Some(_)) => true
    case (false, None)   => true
    case _               => false
  }

  private def z[T]: ((Boolean, Option[T])) => Option[T] = { case (_, value) =>
    value
  }

  private def t[T]: Option[T] => (Boolean, Option[T]) = { value =>
    (value.isDefined, value)
  }

}
