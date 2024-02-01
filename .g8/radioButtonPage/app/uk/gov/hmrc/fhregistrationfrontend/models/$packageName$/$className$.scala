package uk.gov.hmrc.fhregistrationfrontend.models.$packageName$

import play.api.i18n.Messages
import uk.gov.hmrc.fhregistrationfrontend.forms.models.Enumerable
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import viewmodels.WithName
sealed trait $className$Model

object $className$Model extends Enumerable.Implicits {

  case object $option1key;format="Camel"$ extends WithName("$option1key;format="decap"$") with $className$Model
  case object $option2key;format="Camel"$ extends WithName("$option2key;format="decap"$") with $className$Model

  val values: Seq[$className$Model] = Seq(
    $option1key;format="Camel"$, $option2key;format="Camel"$
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map {
    case (value, index) =>
      RadioItem(
        content = Text(messages(s"fh.$packageName$.$className;format="decap"$.\${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_\$index")
      )
  }

  implicit val enumerable: Enumerable[$className$Model] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
