package uk.gov.hmrc.fhregistrationfrontend.controllers

import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import play.api.http.HeaderNames
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.DefaultWSCookie
import play.api.test.WsTestClient
import uk.gov.hmrc.fhregistrationfrontend.forms.models.BusinessPartnerType
import uk.gov.hmrc.fhregistrationfrontend.models.des.PartnerType
import uk.gov.hmrc.fhregistrationfrontend.pages.businessPartners.PartnerTypePage
import uk.gov.hmrc.fhregistrationfrontend.testsupport.{Specifications, TestConfiguration}

class BusinessPartnerControllerISpec
  extends Specifications with TestConfiguration {

  val index = 1
  val modes = List(NormalMode, CheckMode)

  def expectedRedirectLocationForPartnerType(partnerType: BusinessPartnerType.Value): String = {
    partnerType match {
      case BusinessPartnerType.UnincorporatedBody => routes.BusinessPartnersUnincorporatedBodyNameController.load().url
      case BusinessPartnerType.Partnership =>
        routes.BusinessPartnersPartnershipNameController.load(index, NormalMode).url
      case BusinessPartnerType.LimitedLiabilityPartnership =>
        routes.BusinessPartnersLtdLiabilityPartnershipNameController.load(index, NormalMode).url
      case BusinessPartnerType.CorporateBody =>
        routes.BusinessPartnersCorporateBodyCompanyNameController.load(index, NormalMode).url
      case _ =>
        routes.BusinessPartnersIndividualsAndSoleProprietorsPartnerNameController.load(index, NormalMode).url
    }
  }


  modes.foreach { mode =>
    val route = routes.BusinessPartnersController.load(1, mode)
    s"GET ${route.url}" should {
      "render the business partners page" that {
        "has the no items selected" when {
          "there is no useranswers" in {
            given.commonPrecondition
            val result = buildRequestFromRoute(route)
              .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
              .get()

            whenReady(result) { res =>
              res.status mustBe 200
              val page = Jsoup.parse(res.body)
              page.title must include("Business partners - Apply for the Fulfilment House Due Diligence Scheme")
            }
          }
        }

        BusinessPartnerType.values.foreach { partnerType =>

          s"has $partnerType selected" when {
            s"the user has previously selected $partnerType" in {
              given.commonPrecondition
              val userAnswers = emptyUserAnswers.set(PartnerTypePage(1), partnerType).success.value
              addUserAnswersToSession(userAnswers)

              val result = buildRequestFromRoute(route)
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .get()

              whenReady(result) { res =>
                res.status mustBe 200
                val page = Jsoup.parse(res.body)
                page.title must include("Business partners - Apply for the Fulfilment House Due Diligence Scheme")
              }
            }
          }
        }
      }
    }

    s"POST ${route.url}" when {
      BusinessPartnerType.partnerTypes.foreach { partnerType =>
        s"the user selects ${partnerType.toString}" should {
          val expectedUrl = expectedRedirectLocationForPartnerType(partnerType)
          s"redirect to $expectedUrl" when {
            "the the useranswers does not exist" in {
              given.commonPrecondition

              val result = buildRequestFromRoute(route)
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                .post(Map(
                  ("businessPartnersType" -> Seq(partnerType.toString))
                ))

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                val updatedPartnerType = getUserAnswersFromSession.fold[Option[BusinessPartnerType.Value]](None)(_.get(PartnerTypePage(1)))
                updatedPartnerType mustBe Some(partnerType)
              }
            }
          }

          s"override the useranswers and redirect to $expectedUrl" when {
            BusinessPartnerType.partnerTypes.filterNot(_ == partnerType).foreach { savedPartnerType =>
              s"the user answers exist with partner type $savedPartnerType" in {
                given.commonPrecondition
                val userAnswers = emptyUserAnswers.set(PartnerTypePage(1), savedPartnerType).success.value
                addUserAnswersToSession(userAnswers)
                val result = buildRequestFromRoute(route)
                  .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                  .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                  .post(Map(
                    ("businessPartnersType" -> Seq(partnerType.toString))
                  ))

                whenReady(result) { res =>
                  res.status mustBe 303
                  res.header(HeaderNames.LOCATION) mustBe Some(expectedUrl)
                  val updatedPartnerType = getUserAnswersFromSession.fold[Option[BusinessPartnerType.Value]](None)(_.get(PartnerTypePage(1)))
                  updatedPartnerType mustBe Some(partnerType)
                  updatedPartnerType.contains(savedPartnerType) mustBe false
                }
              }
            }
          }


          val noChangeExpectedUrl = if (mode == CheckMode) {
            routes.BusinessPartnersCheckYourAnswersController.load().url
          } else {
            expectedUrl
          }

          s"not update the user answers and redirect to $noChangeExpectedUrl" when {
            s"the user answers partner name is $partnerType" in {
              given.commonPrecondition
              val userAnswers = emptyUserAnswers.set(PartnerTypePage(1), partnerType).success.value
              addUserAnswersToSession(userAnswers)
              val result = buildRequestFromRoute(route)
                .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
                .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
                .post(Map(
                  ("businessPartnersType" -> Seq(partnerType.toString))
                ))

              whenReady(result) { res =>
                res.status mustBe 303
                res.header(HeaderNames.LOCATION) mustBe Some(noChangeExpectedUrl)
                val updatedPartnerType = getUserAnswersFromSession.fold[Option[BusinessPartnerType.Value]](None)(_.get(PartnerTypePage(1)))
                updatedPartnerType mustBe Some(partnerType)
              }
            }
          }
        }
      }

      "return 400" when {
        "the user is authenticated and Nothing is selected" in {
          given.commonPrecondition

          val result = buildRequestFromRoute(route)
            .addCookies(DefaultWSCookie("mdtp", authAndSessionCookie))
            .withHttpHeaders(xSessionId, "Csrf-Token" -> "nocheck")
            .post(Map("businessPartnersType" -> Seq.empty))

          whenReady(result) { res =>
            res.status mustBe 400
            val page = Jsoup.parse(res.body)
            page.getElementsByClass("govuk-error-summary").text() must include("There is a problem Select the type of legal entity for the first partner")
          }
        }
      }
    }
  }
}
