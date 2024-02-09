package uk.gov.hmrc.fhregistrationfrontend.models.$packageName$

import play.api.i18n.Messages
import play.api.libs.json.{Format, Reads, Writes}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

object $className$ extends Enumeration {

  val $option1key$, $option2key$ = Value

  val typeValues: Seq[$className$.Value] = Seq(
    $option1key$, $option2key$
  )

  def options(implicit messages: Messages): Seq[RadioItem] = typeValues.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"fh.$packageName$.$className;format="decap"$.$option1key;format="lowerCase"$.label")),
        value   = Some(value.toString),
        id      = Some(s"value_\$index")
      )
  }

  implicit val format: Format[$className$.Value] =
    Format[$className$.Value](Reads.enumNameReads($className$), Writes.enumNameWrites)

}
