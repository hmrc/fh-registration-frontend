package uk.gov.hmrc.fhregistrationfrontend.forms.deregistration
import play.api.libs.json.{Format, Reads, Writes}

object DeregistrationReasonEnum extends Enumeration {
  type DeregistrationReasonEnum = Value

  val NoLongerNeeded = Value("No longer needed")
  val StoppedTrading = Value("Stopped trading")
  val ChangedLegalEntity = Value("Change of legal entity")
  val Other = Value("Other")

  implicit val format = Format(
    Reads.enumNameReads(DeregistrationReasonEnum),
    Writes.enumNameWrites[this.type ]
  )

}
