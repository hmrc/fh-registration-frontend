package uk.gov.hmrc.fhregistrationfrontend.forms.definitions


import play.api.data.Form
import play.api.data.Forms.mapping
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.localDate
import uk.gov.hmrc.fhregistrationfrontend.forms.models.DateOfIncorporation

object DateOfIncorporationForm {

  val dateOfIncorporationForm = Form(
    mapping(
      "value" → localDate
    )(DateOfIncorporation.apply)(DateOfIncorporation.unapply)
  )

}
