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

package uk.gov.hmrc.fhregistrationfrontend.forms.journey

class LinearJourney {

  val sequence = List[Page[_]](
    Page.mainBusinessAddressPage,
    Page.contactPersonPage,
    Page.companyRegistrationNumberPage,
    Page.dateOfIncorporationPage
  )

  def get[T](pageId: String): Option[Page[T]] = {
    sequence find ( _.id == pageId) map (_.asInstanceOf[Page[T]])
  }

  def next(pageId: String): Option[Page[_]] = {
    sequence dropWhile (_.id != pageId) match {
      case page :: next :: rest ⇒ Some(next)
      case _                    ⇒ None
    }
  }

  def previous(pageId: String): Option[Page[_]] = {
    sequence takeWhile (_.id != pageId) lastOption
  }


}
