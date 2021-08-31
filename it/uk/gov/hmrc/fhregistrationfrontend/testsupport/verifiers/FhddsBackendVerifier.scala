package uk.gov.hmrc.fhregistrationfrontend.testsupport.verifiers

import org.scalatest.matchers.should.Matchers
import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.{JsObject, Json}

import scala.collection.JavaConverters._

class FhddsBackendVerifier ()(implicit builder: VerifierBuilder) extends Matchers {

  def amendWasCalled = {
    val requests  = findAll(postRequestedFor(urlMatching("/fhdds/subscription/amend/..FH[0-9A-Z]+"))).asScala
    requests.size shouldBe 1
    builder
  }


  def contactEmailMatches(email: String) = {
    val requests  = findAll(postRequestedFor(urlMatching("/fhdds/subscription/amend/..FH[0-9A-Z]+"))).asScala
    val result = Json.parse(requests(0).getBody).as[JsObject] \ "submission" \ "subScriptionCreate" \ "contactDetail" \ "commonDetails" \ "email"

    result.asOpt[String] shouldBe Some(email)
    builder
  }

  def contactDetailChangedFlag(expected: Boolean) = {
    val requests  = findAll(postRequestedFor(urlMatching("/fhdds/subscription/amend/..FH[0-9A-Z]+"))).asScala
    val result = Json.parse(requests(0).getBody).as[JsObject] \ "submission" \ "changeIndicators" \ "contactDetailChanged"

    result.asOpt[Boolean] shouldBe Some(expected)
    builder


  }

}
