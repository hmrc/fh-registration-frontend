package uk.gov.hmrc.fhregistrationfrontend.actions

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.mvc._
import play.api.mvc.Results._
import play.api.test.Helpers._
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.fhregistrationfrontend.config.{ErrorHandler, FrontendAppConfig}
import uk.gov.hmrc.fhregistrationfrontend.connectors.{ExternalUrls, FhddsConnector}
import uk.gov.hmrc.fhregistrationfrontend.forms.journey.Journeys
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository
import uk.gov.hmrc.fhregistrationfrontend.services.Save4LaterService

import scala.concurrent.{ExecutionContext, Future}

class ActionsSpec extends AsyncWordSpec with Matchers with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val externalUrls       = mock[ExternalUrls]
  private val fhddsConnector     = mock[FhddsConnector]
  private val sessionRepository  = mock[SessionRepository]
  private val frontendAppConfig  = mock[FrontendAppConfig]
  private val cc: ControllerComponents = stubControllerComponents()
  private val journeys           = mock[Journeys]
  private implicit val authConnector: AuthConnector = mock[AuthConnector]
  private implicit val save4LaterService: Save4LaterService = mock[Save4LaterService]
  private implicit val errorHandler: ErrorHandler = mock[ErrorHandler]

  private val actions = new Actions(
    externalUrls,
    fhddsConnector,
    sessionRepository,
    frontendAppConfig,
    cc,
    journeys
  )

  private val fakeUserRequest =
    new UserRequest("user-123", Some("email@test.com"), None, None, None, FakeRequest("GET", "/"))

  "newBusinessPartnersFlowEnabledAction" should {
    "allow request through when feature is enabled" in {
      when(frontendAppConfig.newBusinessPartnerPagesEnabled).thenReturn(true)

      val dummyActionBuilder = new ActionBuilder[UserRequest, AnyContent] {
        override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

        override protected def executionContext: ExecutionContext = ec

        override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] =
          block(fakeUserRequest.asInstanceOf[UserRequest[A]])
      }

      val action =
        (dummyActionBuilder andThen actions.newBusinessPartnersFlowEnabledAction).async { _ =>
          Future.successful(Ok("allowed"))
        }

      val result = action(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) shouldBe "allowed"
    }

    "block request when feature is disabled" in {
      when(frontendAppConfig.newBusinessPartnerPagesEnabled).thenReturn(false)
      when(errorHandler.errorResultsPages(
        any[Results.Status],
        any[Option[String]])(using any[UserRequest[?]])
      ).thenReturn(NotFound("Business partners flow disabled"))

      val dummyActionBuilder = new ActionBuilder[UserRequest, AnyContent] {
        override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

        override protected def executionContext: ExecutionContext = ec

        override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] =
          block(fakeUserRequest.asInstanceOf[UserRequest[A]])
      }

      val action =
        (dummyActionBuilder andThen actions.newBusinessPartnersFlowEnabledAction).async { _ =>
          Future.successful(Ok("allowed"))
        }

      val result = action(FakeRequest())
      status(result) shouldBe NOT_FOUND
      contentAsString(result) should include("Business partners flow disabled")
    }
  }

  "newCompanyOfficersFlowEnabledAction" should {
    "allow request through when feature is enabled" in {
      when(frontendAppConfig.newCompanyOfficersPagesEnabled).thenReturn(true)

      val dummyActionBuilder = new ActionBuilder[UserRequest, AnyContent] {
        override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

        override protected def executionContext: ExecutionContext = ec

        override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] =
          block(fakeUserRequest.asInstanceOf[UserRequest[A]])
      }

      val action =
        (dummyActionBuilder andThen actions.newCompanyOfficersFlowEnabledAction).async { _ =>
          Future.successful(Ok("allowed"))
        }

      val result = action(FakeRequest())
      status(result) shouldBe OK
      contentAsString(result) shouldBe "allowed"
    }

    "block request when feature is disabled" in {
      when(frontendAppConfig.newCompanyOfficersPagesEnabled).thenReturn(false)
      when(errorHandler.errorResultsPages(
        any[Results.Status],
        any[Option[String]])(using any[UserRequest[?]])
      ).thenReturn(NotFound("Company officers flow disabled"))

      val dummyActionBuilder = new ActionBuilder[UserRequest, AnyContent] {
        override def parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser

        override protected def executionContext: ExecutionContext = ec

        override def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] =
          block(fakeUserRequest.asInstanceOf[UserRequest[A]])
      }

      val action =
        (dummyActionBuilder andThen actions.newCompanyOfficersFlowEnabledAction).async { _ =>
          Future.successful(Ok("allowed"))
        }

      val result = action(FakeRequest())
      status(result) shouldBe NOT_FOUND
      contentAsString(result) should include("Company officers flow disabled")
    }
  }


  "userAction" should {
    "be an ActionBuilder" in {
      actions.userAction shouldBe a[ActionBuilder[?, ?]]
    }
  }

  "dataRetrievalAction" should {
    "be an ActionBuilder" in {
      actions.dataRetrievalAction shouldBe a[ActionBuilder[?, ?]]
    }
  }

  "pageAction" should {
    "return an ActionBuilder" in {
      actions.pageAction("pageId") shouldBe a[ActionBuilder[?, ?]]
      actions.pageAction("pageId", Some("sectionId")) shouldBe a[ActionBuilder[?, ?]]
    }
  }

  "summaryAction" should {
    "return an ActionBuilder" in {
      actions.summaryAction shouldBe a[ActionBuilder[?, ?]]
    }
  }

  "newApplicationAction" should {
    "return an ActionBuilder" in {
      actions.newApplicationAction shouldBe a[ActionBuilder[?, ?]]
    }
  }
}
