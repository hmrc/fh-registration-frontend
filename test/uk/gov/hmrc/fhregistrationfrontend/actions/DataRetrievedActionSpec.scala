package uk.gov.hmrc.fhregistrationfrontend.actions

import models.UserAnswers
import org.mockito.Mockito.*
import org.scalatest.matchers.must.Matchers.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.test.FakeRequest
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.auth.core.User
import uk.gov.hmrc.fhregistrationfrontend.repositories.SessionRepository

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievedActionSpec extends AsyncWordSpec with Matchers {

  val userId = "user-123"
  val userAnswers = UserAnswers(userId, Map.empty, Instant.now)

  val fakeRequest = FakeRequest("GET", "/test")
  val userRequest = new UserRequest(
    userId = userId,
    ggEmail = Some("test@example.com"),
    registrationNumber = Some("FH123456"),
    credentialRole = Some(User),
    userAffinityGroup = Some(Individual),
    request = fakeRequest
  )

  val mockSessionRepo: SessionRepository = mock(classOf[SessionRepository])

  // Subclass to expose the protected transform method
  class TestableDataRetrievedAction extends DataRetrievedAction(mockSessionRepo) {
    def callTransform[A](req: UserRequest[A]): Future[DataRetrievedActionRequest[A]] =
      transform(req)
  }

  val action = new TestableDataRetrievedAction

  "DataRetrievedAction" should {

    "return DataRetrievedActionRequest with user answers when session data exists" in {
      when(mockSessionRepo.get(userId)).thenReturn(Future.successful(Some(userAnswers)))

      val result = action.callTransform(userRequest)

      result.map { dataRetrievedRequest =>
        dataRetrievedRequest.optUserAnswers mustBe Some(userAnswers)
        dataRetrievedRequest.optUserAnswers.get mustBe userAnswers
        dataRetrievedRequest.userId mustBe userId
      }
    }

    "return DataRetrievedActionRequest with None when session data does not exist" in {
      when(mockSessionRepo.get(userId)).thenReturn(Future.successful(None))

      val result = action.callTransform(userRequest)

      result.map { dataRetrievedRequest =>
        dataRetrievedRequest.optUserAnswers mustBe None
        dataRetrievedRequest.userId mustBe userId
      }
    }
  }
}
