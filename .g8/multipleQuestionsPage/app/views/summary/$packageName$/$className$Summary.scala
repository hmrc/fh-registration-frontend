package viewmodels.summary.$packageName$

import controllers.$packageName$.routes
import models.{CheckMode, UserAnswers}
import pages.$packageName$.$className$Page
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object $className$Summary  {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get($className$Page).map {
      answer =>

      val value = HtmlFormat.escape(answer.$field1Name$).toString + "<br/>" + HtmlFormat.escape(answer.$field2Name$).toString

        SummaryListRowViewModel(
          key     = "$packageName$.$className;format="decap"$.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlContent(value)),
          actions = Seq(
            ActionItemViewModel("site.change", routes.$className$Controller.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("$packageName$.$className;format="decap"$.change.hidden"))
          )
        )
    }
}
