package uk.gov.hmrc.fhregistrationfrontend.viewmodels.summary.$packageName$

import uk.gov.hmrc.fhregistrationfrontend.controllers.$packageName$.routes
import models.{CheckMode, UserAnswers}
import uk.gov.hmrc.fhregistrationfrontend.models.$packageName$.$className$Page
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow



object $className$Summary {
  def row(index: Int, answers: UserAnswers)(implicit messages: Messages): SummaryListRow = {
    answers.get($className$Page).map {
      answer =>

        val value = ValueViewModel(
          HtmlContent(
            answers.map {
                answer => HtmlFormat.escape(messages(s"$packageName$.$className;format="decap"$.\$answer")).toString
              }
              .mkString(",<br>")
          )
        )

      SummaryListRowViewModel(
        key = "fh.$packageName$.$className;format="
      decap"$.checkYourAnswersLabel"
      ,
      value = ValueViewModel(HtmlContent(value))
      ,
      actions = Seq(
        ActionItemViewModel("link.change", routes.$className$Controller.onPageLoad(index, CheckMode).url)
          .withVisuallyHiddenText(messages("fh.$packageName$.$className;format="
      decap"$.change.hidden"
      ) )
      )
      )
    }
  }
}
