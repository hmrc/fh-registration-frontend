/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers}
import play.api.http.Status
import play.api.mvc.Results
import uk.gov.hmrc.fhregistrationfrontend.teststubs.UserTestData
import uk.gov.hmrc.fhregistrationfrontend.util.UnitSpec

trait ControllersSpecBase
    extends UnitSpec with ScalaFutures with MockitoSugar with BeforeAndAfterEach with Matchers with Results with Status
    with UserTestData {}
