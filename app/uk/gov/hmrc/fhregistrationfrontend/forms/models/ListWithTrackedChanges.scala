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

package uk.gov.hmrc.fhregistrationfrontend.forms.models

import julienrf.json.derived
import play.api.libs.json._
import uk.gov.hmrc.fhregistrationfrontend.forms.models.ListWithTrackedChanges._

object ListWithTrackedChanges {

  sealed trait Status
  case object NoChange extends Status
  case object Updated extends Status
  case object Added extends Status

  implicit val statusFormat: OFormat[Status] = derived.oformat[Status]()
  def empty[T](): ListWithTrackedChanges[T] = ListWithTrackedChanges[T](List.empty, List.empty, false)

  def fromValues[T](values: List[T]): ListWithTrackedChanges[T] = ListWithTrackedChanges(
    values zip Iterable.fill(values.size)(NoChange),
    List.empty,
    false
  )

  implicit def valueWithStatusFormat[T](implicit format: Format[T]): OFormat[(T, Status)] = Json.format[(T, Status)]

  implicit private def writes[T](implicit format: Format[T]): Writes[ListWithTrackedChanges[T]] =
    new Writes[ListWithTrackedChanges[T]] {
      override def writes(o: ListWithTrackedChanges[T]): JsValue = Json obj (
        "valuesWithStatus" -> o.valuesWithStatus,
        "deleted"          -> o.deleted,
        "addMore"          -> o.addMore
      )
    }

  implicit private def reads[T](implicit format: Format[T]): Reads[ListWithTrackedChanges[T]] =
    new Reads[ListWithTrackedChanges[T]] {
      override def reads(json: JsValue): JsResult[ListWithTrackedChanges[T]] =
        for {
          values  <- (json \ "valuesWithStatus").validate[List[(T, Status)]]
          deleted <- (json \ "deleted").validate[List[T]]
          addMore <- (json \ "addMore").validateOpt[Boolean].map(_ getOrElse false)
        } yield {
          ListWithTrackedChanges(values, deleted, addMore)
        }
    }

  implicit def listWithTrackedChangesFormat[T](implicit format: Format[T]): Format[ListWithTrackedChanges[T]] =
    Format(reads, writes)
}

case class ListWithTrackedChanges[T](valuesWithStatus: List[(T, Status)], deleted: List[T], addMore: Boolean) {

  def append(value: T) = this copy (valuesWithStatus = valuesWithStatus :+ (value -> Added))

  def values = valuesWithStatus.iterator map (_._1)

  def remove(index: Int) = {
    val (value, status) = valuesWithStatus(index)
    val newDeleted = if (status != Added) value :: deleted else deleted

    val newValues = valuesWithStatus.zipWithIndex collect {
      case (v, i) if index != i => v
    }

    this copy (
      valuesWithStatus = newValues,
      deleted = newDeleted
    )
  }

  def apply(index: Int) = valuesWithStatus(index)._1

  def updated(index: Int, value: T) = {
    val prevValue = valuesWithStatus(index)._1
    if (prevValue != value)
      this copy (valuesWithStatus = valuesWithStatus updated (index, value -> Updated))
    else
      this
  }

  def size = valuesWithStatus.size
}
