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

package uk.gov.hmrc.fhregistrationfrontend.config

import javax.inject.{Inject, Singleton}
import com.google.inject.ImplementedBy
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.util.Random

@ImplementedBy(classOf[FrontendAppConfig])
trait AppConfig {
  val reportAProblemNonJSUrl: String
  val exitSurveyUrl: String
  val appName: String
  def emailVerificationCallback(hash: String): String
  val exciseEnquiryLink: String
  def getConfiguration: Configuration

  val newBusinessPartnerPagesEnabled: Boolean
  val newCompanyOfficersPagesEnabled: Boolean

  val isNewSummaryConfirmationCacheEnabled: Boolean
  val isNewSessionRepositoryCacheEnabled: Boolean

  def serviceMaxNoOfAttempts: Int
}

@Singleton
class FrontendAppConfig @Inject() (
  configuration: play.api.Configuration,
  val runModeConfiguration: Configuration,
  servicesConfig: ServicesConfig,
  environment: Environment
) extends ServicesConfig(runModeConfiguration) with AppConfig {

  private def loadConfig(key: String) =
    configuration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  private lazy val _serviceMaxNoOfAttempts: Int = getConfString("fhdds-service_max_no_of_attempts", "").toInt

  lazy val contactFrontend: String = getConfString("contact-frontend-url-base", "")
  lazy val fhddsFrontendUrl: String = getConfString("fhdds-frontend-url-base", "/fhdds")
  val addressReputationEndpoint = baseUrl("address-lookup")
  override lazy val exciseEnquiryLink: String = getConfString(
    "exciseEnquiryLink",
    "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/excise-enquiries"
  )

  override lazy val appName: String = loadConfig("appName")

  private val contactFormServiceIdentifier = appName
  override lazy val reportAProblemNonJSUrl =
    s"$contactFrontend/contact/report-technical-problem?service=$contactFormServiceIdentifier"
  override lazy val exitSurveyUrl: String =
    s"$contactFrontend/contact/beta-feedback?service=$contactFormServiceIdentifier"

  override def emailVerificationCallback(hash: String) =
    s"$fhddsFrontendUrl/email-verify/$hash"

  lazy val username = getString("credentials.username")
  lazy val password = getString("credentials.password")

  lazy val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  override def getConfiguration: Configuration = configuration

  override lazy val newBusinessPartnerPagesEnabled: Boolean = getBoolean("business-partners-new-enabled")

  override lazy val newCompanyOfficersPagesEnabled: Boolean = getBoolean("company-officers-new-enabled")

  override val isNewSummaryConfirmationCacheEnabled: Boolean = getBoolean("isNewSummaryConfirmationCacheEnabled")
  override val isNewSessionRepositoryCacheEnabled: Boolean = getBoolean("isNewSessionRepositoryCacheEnabled")
  override def serviceMaxNoOfAttempts: Int = _serviceMaxNoOfAttempts

  // TODO [DLS-7603] - temp save4later solution remove when cookies removed from load function
  val staticBusinessTypes: Seq[String] =
    Seq(
      "partnership",
      "limited-liability-partnership",
      "sole-proprietor",
      "individual",
      "corporateBody",
      "unincorporated-body"
    )
  def getRandomBusinessType(): String = staticBusinessTypes(Random.nextInt(6))

  val vatNumber = Seq(true, false)
  def hasVatNumber(): Boolean = vatNumber(Random.nextInt(2))

}
