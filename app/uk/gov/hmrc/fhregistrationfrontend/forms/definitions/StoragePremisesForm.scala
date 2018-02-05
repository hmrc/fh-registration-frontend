package uk.gov.hmrc.fhregistrationfrontend.forms.definitions


import play.api.data.Form
import play.api.data.Forms.{list, mapping, nonEmptyText, optional, of}
import uk.gov.hmrc.fhregistrationfrontend.forms.models._
import uk.gov.hmrc.fhregistrationfrontend.models.formmodel.CustomFormatters.radioButton
import uk.gov.hmrc.fhregistrationfrontend.forms.mappings.Mappings.{localDate, address}

object StoragePremisesForm {

  val storagePremisesForm = Form(
    mapping(
     "premises" → list(storagePrmiseMapping)
    )(StoragePremises.apply)(StoragePremises.unapply)
  )


  val storagePrmiseMapping = mapping(
    "address" → address,
    "isThirdParty" → of(radioButton)
  )(StoragePremise.apply)(StoragePremise.unapply)
}
