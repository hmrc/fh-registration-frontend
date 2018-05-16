package uk.gov.hmrc.fhregistrationfrontend.testsupport.actions

class ActionsBuilder(baseUrl: String) {

  implicit val builder: ActionsBuilder = this

  def calls = new ServiceCalls(baseUrl)

}
