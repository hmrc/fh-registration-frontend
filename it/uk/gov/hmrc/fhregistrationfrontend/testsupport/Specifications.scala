package uk.gov.hmrc.fhregistrationfrontend.testsupport

import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.fhregistrationfrontend.testsupport.actions.ActionsBuilder
import uk.gov.hmrc.fhregistrationfrontend.testsupport.preconditions.PreconditionBuilder
import uk.gov.hmrc.fhregistrationfrontend.testsupport.verifiers.VerifierBuilder

trait Specifications extends PlaySpec with AnyWordSpecLike with ScalaFutures {
  this: TestConfiguration =>

  implicit val `given`: PreconditionBuilder = new PreconditionBuilder
  implicit val expect: VerifierBuilder = new VerifierBuilder
  lazy val user = new ActionsBuilder(baseUrl)

}
