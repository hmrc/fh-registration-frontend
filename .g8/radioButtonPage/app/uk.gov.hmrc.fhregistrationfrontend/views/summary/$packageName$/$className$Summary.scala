package uk.gov.hmrc.fhregistrationfrontend.viewmodels.summary.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.routes
import uk.gov.hmrc.fhregistrationfrontend.models.{CheckMode, UserAnswers}
import uk.gov.hmrc.fhregistrationfrontend.pages.$packageName$.$className$Page
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.fhregistrationfrontend.viewmodels.govuk.summarylist._
import uk.gov.hmrc.fhregistrationfrontend.viewmodels.implicits._

object $className$Summary  {

  def row($className$;format="decap": $classname$)(implicit messages: Messages): SummaryListRow =

    val value = HtmlFormat.escape(answer.$fieldName$).toString + "<br/>" + HtmlFormat.escape(answer.$fieldName2$).toString
    SummaryListRowViewModel(
      key     = "fh.$packageName$.$className;format="decap"$.checkYourAnswersLabel",
      value   = ValueViewModel(HtmlContent(value)),
      actions = Seq(
        ActionItemViewModel("link.change", routes.$className$Controller.onPageLoad(CheckMode).url)
          .withVisuallyHiddenText(messages("$packageName$.$className;format="decap"$.change.hidden"))
      )
    )
    }
}
