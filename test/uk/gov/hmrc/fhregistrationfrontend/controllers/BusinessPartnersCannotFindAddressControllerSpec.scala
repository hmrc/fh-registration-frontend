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

package uk.gov.hmrc.fhregistrationfrontend.controllers

import com.codahale.metrics.SharedMetricRegistries
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.mockito.Mockito.{reset, when}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import uk.gov.hmrc.fhregistrationfrontend.config.FrontendAppConfig
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerType
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnerTypePage
import uk.gov.hmrc.fhregistrationfrontend.teststubs.ActionsMock
import uk.gov.hmrc.fhregistrationfrontend.views.Views

class BusinessPartnersCannotFindAddressControllerSpec extends ControllerSpecWithGuiceApp with ActionsMock {

  SharedMetricRegistries.clear()

  override lazy val views: Views = app.injector.instanceOf[Views]
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val controller =
    new BusinessPartnersCannotFindAddressController(commonDependencies, views, mockActions, mockAppConfig)(mockMcc)

  def enterPartnerRegOfficeAddressUrl(mode: Mode): String =
    routes.BusinessPartnersPartnershipEnterAddressController.load(index, mode).url
  def enterPartnerAddressUrl(mode: Mode): String = routes.BusinessPartnersEnterAddressController.load(1, mode).url
  val enterCorpBodyRegOfficeAddressUrl: String = routes.BusinessPartnersCorporateBodyEnterAddressController.load().url
  val enterUnincorpBodyRegOfficeAddressUrl: String =
    routes.BusinessPartnersUnincorporatedBodyEnterAddressController.load().url
  val index = 1
  val emptyUA = UserAnswers("some-id")

  List(NormalMode, CheckMode).foreach { mode =>
    s"load when in $mode" should {
      "Render the Cannot Find Address page" when {
        "the user answers contain businessType limited-liability-partnership" in {
          val userAnswers = UserAnswers(testUserId)
            .set(PartnerTypePage(index), BusinessPartnerType.LimitedLiabilityPartnership)
            .success
            .value
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("We cannot find any addresses for HR33 7GP")
          // should be mocked out when Save4Later changes included
          page.getElementById("enter-manually").attr("href") should include(enterPartnerRegOfficeAddressUrl(mode))
          reset(mockActions)
        }

        "the businessType returned is a sole-proprietor" in {
          val userAnswers = UserAnswers(testUserId)
            .set(PartnerTypePage(index), BusinessPartnerType.SoleProprietor)
            .success
            .value
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("We cannot find any addresses for HR33 7GP")
          // should be mocked out when Save4Later changes included
          page.getElementById("enter-manually").attr("href") should include(enterPartnerAddressUrl(mode))
          reset(mockActions)
        }

        "the businessType returned is a individual" in {
          val userAnswers = UserAnswers(testUserId)
            .set(PartnerTypePage(index), BusinessPartnerType.Individual)
            .success
            .value
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("We cannot find any addresses for HR33 7GP")
          // should be mocked out when Save4Later changes included
          page.getElementById("enter-manually").attr("href") should include(enterPartnerAddressUrl(mode))
          reset(mockActions)
        }

        "the businessType returned is a corporateBody" in {
          val userAnswers = UserAnswers(testUserId)
            .set(PartnerTypePage(index), BusinessPartnerType.CorporateBody)
            .success
            .value
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("We cannot find any addresses for HR33 7GP")
          // should be mocked out when Save4Later changes included
          page.getElementById("enter-manually").attr("href") should include(enterCorpBodyRegOfficeAddressUrl)
          reset(mockActions)
        }

        "the businessType returned is a unincorporated-body" in {
          val userAnswers = UserAnswers(testUserId)
            .set(PartnerTypePage(index), BusinessPartnerType.UnincorporatedBody)
            .success
            .value
          setupDataRequiredAction(userAnswers, mode)

          val request = FakeRequest()
          val result = await(csrfAddToken(controller.load(index, mode))(request))

          status(result) shouldBe OK
          val page = Jsoup.parse(contentAsString(result))
          page.title() should include("We cannot find any addresses for HR33 7GP")
          // should be mocked out when Save4Later changes included
          page.getElementById("enter-manually").attr("href") should include(enterUnincorpBodyRegOfficeAddressUrl)
          reset(mockActions)
        }
      }
    }
  }
}
