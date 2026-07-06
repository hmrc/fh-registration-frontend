/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.fhregistrationfrontend.repositories

import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.fhregistrationfrontend.forms.deregistration.{DeregistrationReason, DeregistrationReasonEnum}
import uk.gov.hmrc.fhregistrationfrontend.forms.withdrawal.{WithdrawalReason, WithdrawalReasonEnum}
import uk.gov.hmrc.fhregistrationfrontend.models.SummaryConfirmation
import org.mongodb.scala.SingleObservableFuture

import java.util.concurrent.TimeUnit

class SummaryConfirmationRepositoryISpec
    extends AnyFreeSpec with Matchers with ScalaFutures with IntegrationPatience with OptionValues
    with GuiceOneAppPerSuite with FutureAwaits with DefaultAwaitTimeout with BeforeAndAfterEach {

  val repository: SummaryConfirmationRepository = app.injector.instanceOf[SummaryConfirmationRepository]
  val id: String = "id"
  val summaryForPrintKey: String = "testData"
  val withdrawalReason: WithdrawalReason = WithdrawalReason(WithdrawalReasonEnum.NoLongerApplicable, Some("testData"))
  val deregistrationReason: DeregistrationReason =
    DeregistrationReason(DeregistrationReasonEnum.NoLongerNeeded, Some("testData"))
  val summaryConfirmation: SummaryConfirmation =
    new SummaryConfirmation(id, Some(summaryForPrintKey), Some(withdrawalReason), Some(deregistrationReason))

  override def beforeEach(): Unit = {
    await(repository.collection.deleteMany(BsonDocument()).toFuture())
    super.beforeEach()
  }

  "indexes" - {
    "are correct" in {
      repository.indexes.toList.toString() mustBe List(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(900, TimeUnit.SECONDS)
        )
      ).toString()
    }
  }

  ".set" - {
    "Must successfully save a record to the DB" - {
      val result = await(repository.set(summaryConfirmation))

      result mustEqual true

      val insertedModel = await(repository.get(summaryConfirmation.id)).get
      insertedModel.id mustBe summaryConfirmation.id
      insertedModel.summaryForPrintKey mustBe summaryConfirmation.summaryForPrintKey
      insertedModel.withdrawalReason mustBe summaryConfirmation.withdrawalReason
      insertedModel.deregistrationReason mustBe summaryConfirmation.deregistrationReason
    }
  }

  ".get" - {
    "when there is a record for this id" - {
      "must return the correct record" in {
        await(repository.set(summaryConfirmation))

        val insertedRecord = await(repository.get(summaryConfirmation.id)).get
        insertedRecord.id mustBe summaryConfirmation.id
        insertedRecord.summaryForPrintKey mustBe summaryConfirmation.summaryForPrintKey
        insertedRecord.withdrawalReason mustBe summaryConfirmation.withdrawalReason
        insertedRecord.deregistrationReason mustBe summaryConfirmation.deregistrationReason
      }
    }

    "when there is no record for this id" - {
      "must return None" in {
        repository.get("id that does not exist").futureValue must not be defined
      }
    }

  }
}
