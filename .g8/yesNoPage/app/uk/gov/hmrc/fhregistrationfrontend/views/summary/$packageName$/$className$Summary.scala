package uk.gov.hmrc.fhregistrationfrontend.viewmodels.summary.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.routes
import models.CheckMode
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object $className$Summary  {

  def row(index: Int, answer: Boolean)(implicit messages: Messages): SummaryListRow = {
    val value = if (answer) "fh.generic.yes" else "fh.generic.no"
      SummaryListRowViewModel(
        key     = "fh.$packageName$.$className;format="decap"$.checkYourAnswersLabel",
        value   = ValueViewModel(value),
        actions = Seq(
          ActionItemViewModel("link.change", routes.$className$Controller.onPageLoad(index, CheckMode).url)
            .withVisuallyHiddenText(messages("fh.$packageName$.$className;format="decap"$.change.hidden"))
        )
      )
    }
}
