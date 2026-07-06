/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.fhregistrationfrontend.testsupport.verifiers

import org.scalatest.matchers.should.Matchers
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.{JsObject, Json}

import scala.jdk.CollectionConverters._

class FhddsBackendVerifier()(implicit builder: VerifierBuilder) extends Matchers {

  def amendWasCalled = {
    val requests = findAll(postRequestedFor(urlMatching("/fhdds/subscription/amend/..FH[0-9A-Z]+"))).asScala
    requests.size shouldBe 1
    builder
  }

  def contactEmailMatches(email: String) = {
    val requests = findAll(postRequestedFor(urlMatching("/fhdds/subscription/amend/..FH[0-9A-Z]+"))).asScala
    val result = Json
      .parse(requests(0).getBody)
      .as[JsObject] \ "submission" \ "subScriptionCreate" \ "contactDetail" \ "commonDetails" \ "email"

    result.asOpt[String] shouldBe Some(email)
    builder
  }

  def contactDetailChangedFlag(expected: Boolean) = {
    val requests = findAll(postRequestedFor(urlMatching("/fhdds/subscription/amend/..FH[0-9A-Z]+"))).asScala
    val result =
      Json.parse(requests(0).getBody).as[JsObject] \ "submission" \ "changeIndicators" \ "contactDetailChanged"

    result.asOpt[Boolean] shouldBe Some(expected)
    builder

  }

}
