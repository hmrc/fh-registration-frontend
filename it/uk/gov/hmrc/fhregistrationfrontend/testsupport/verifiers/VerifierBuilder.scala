package uk.gov.hmrc.fhregistrationfrontend.testsupport.verifiers

class VerifierBuilder {
  implicit val builder: VerifierBuilder = this

  val fhddsBackend = new FhddsBackendVerifier()

}
