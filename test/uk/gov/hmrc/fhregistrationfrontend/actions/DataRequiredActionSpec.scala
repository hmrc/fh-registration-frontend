package uk.gov.hmrc.fhregistrationfrontend.actions

import models.{Mode, NormalMode, UserAnswers}
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.User

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class DataRequiredActionSpec extends AsyncWordSpec with Matchers {

  val mode: Mode = NormalMode
  val index = 0
  val userId = "user-123"
  val userAnswers = UserAnswers(userId, Map.empty, Instant.now())

  val fakeRequest = FakeRequest("GET", "/test")
  val userRequest = new UserRequest[AnyContentAsEmpty.type](
    userId = userId,
    ggEmail = Some("test@example.com"),
    registrationNumber = Some("FH123456"),
    credentialRole = Some(User),
    userAffinityGroup = Some(Individual),
    request = fakeRequest
  )

  // Subclass to expose the protected refine method for testing
  class TestableDataRequiredAction extends DataRequiredAction(global, index, mode) {
    def callRefine[A](request: DataRetrievedActionRequest[A]): Future[Either[Result, DataRequiredRequest[A]]] =
      refine(request)
  }

  val action = new TestableDataRequiredAction

  "DataRequiredAction" should {

    "return Right(DataRequiredRequest) when user answers are present" in {
      val request = new DataRetrievedActionRequest(userRequest, Some(userAnswers))
      val result = action.callRefine(request)

      result.map {
        case Right(dataRequiredRequest) =>
          dataRequiredRequest.userAnswers mustBe userAnswers
          dataRequiredRequest.userId mustBe userId
        case Left(_) => fail("Expected Right(DataRequiredRequest), got Left")
      }
    }

    "return Left(Redirect) when user answers are missing" in {
      val request = new DataRetrievedActionRequest(userRequest, None)
      val result = action.callRefine(request)

      result.map {
        case Left(redirectResult) =>
          redirectResult.header.status mustBe SEE_OTHER
          redirectResult.header.headers(LOCATION) mustBe
            uk.gov.hmrc.fhregistrationfrontend.controllers.routes.Application.main().url
        case Right(_) => fail("Expected Left(Redirect), got Right")
      }
    }
  }
}
